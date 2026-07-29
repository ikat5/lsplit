# L-Split — Complete Deployment Guide

| Piece      | Tech                | Deploys to        |
|------------|---------------------|-------------------|
| Backend    | Spring Boot 3 (Java 17, Docker) | **Render**  |
| Frontend   | React + Vite        | **Vercel**        |
| Database   | PostgreSQL          | **Neon** (already provisioned) |

Everything below has been tested locally against your actual Neon database — the backend
boots, runs Flyway migrations, and the full API flow (register → group → event → expense →
settlement) works.

---

## Part 0 — Your database credentials

Your Neon connection string, converted to the environment variables the backend expects:

```
DB_URL=jdbc:postgresql://ep-tiny-cake-ayt9e7pq-pooler.c-5.us-east-2.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_9Xqu5fJvOLVp
```

Two important details:

- **Do NOT include `&channel_binding=require`** in `DB_URL`. That is a libpq (psql) parameter;
  the Java JDBC driver mishandles it and Neon closes the connection. Keep only `?sslmode=require`.
- **The URL must start with `jdbc:postgresql://`**, not `postgresql://`, and the username/password
  are passed separately (not inside the URL).

You also need a JWT signing secret. Generate a fresh one:

```bash
openssl rand -hex 32
```

> ⚠️ **Security note:** your database password was shared in chat and exists in `backend/.env`.
> After you finish deploying, rotate it in the Neon console (**Dashboard → Roles & Databases →
> Reset password**) and update the env var on Render. `.gitignore` already prevents `.env` from
> being committed.

---

## Part 1 — Push the code to GitHub

Render and Vercel both deploy from a GitHub repo. From the project root:

```bash
cd /home/tarit/Downloads/l-split-v2

# Verify no secrets are staged — this must print nothing except *.example files:
git add -A
git status --short | grep -i "\.env"

git commit -m "Fix bugs, make deploy ready"

# Option A — GitHub CLI:
gh repo create l-split --private --source=. --remote=origin --push

# Option B — manual: create an empty repo at github.com/new, then:
# git remote add origin https://github.com/<your-username>/l-split.git
# git branch -M main
# git push -u origin main
```

After pushing, open the repo on GitHub and confirm `backend/.env` is **not** there
(only `backend/.env.example` should be).

---

## Part 2 — Deploy the backend on Render

1. Go to <https://dashboard.render.com> → **New +** → **Web Service**.
2. Connect your GitHub account and select the **l-split** repo.
3. Configure the service:

   | Setting            | Value |
   |--------------------|-------|
   | Name               | `l-split-backend` (or anything — this becomes your URL) |
   | Region             | **Ohio (US East)** — same region as your Neon DB |
   | Branch             | `main` |
   | Root Directory     | `backend` |
   | Language / Runtime | **Docker** (auto-detected from `backend/Dockerfile`) |
   | Instance Type      | **Free** |

4. Under **Environment Variables**, add these four (do **not** set `PORT` — Render injects it
   and the app picks it up automatically):

   | Key           | Value |
   |---------------|-------|
   | `DB_URL`      | `jdbc:postgresql://ep-tiny-cake-ayt9e7pq-pooler.c-5.us-east-2.aws.neon.tech/neondb?sslmode=require` |
   | `DB_USERNAME` | `neondb_owner` |
   | `DB_PASSWORD` | `npg_9Xqu5fJvOLVp` |
   | `JWT_SECRET`  | output of `openssl rand -hex 32` |

5. (Recommended) Under **Health Check Path**, set: `/api/health`
   — the backend now exposes this endpoint publicly and returns `{"status":"UP"}`.
6. Click **Create Web Service**. The first Docker build takes ~3–5 minutes.
7. When it shows **Live**, copy your backend URL — yours is `https://lsplit.onrender.com`.
8. Verify in a browser: `https://lsplit.onrender.com/api/health` → `{"status":"UP"}`.

> **Free-tier behavior:** Render spins the service down after ~15 min of no traffic; the next
> request takes ~30–60 s while it wakes up (and Neon's free tier may also be waking its compute).
> This is normal. The app's connection-pool settings are already tuned to survive these cold starts.

---

## Part 3 — Deploy the frontend on Vercel

1. Go to <https://vercel.com> → **Add New… → Project** → import the same **l-split** repo.
2. Configure the project:

   | Setting          | Value |
   |------------------|-------|
   | Root Directory   | `frontend`  ← click **Edit** next to the root directory to set this |
   | Framework Preset | **Vite** (auto-detected) |
   | Build Command    | `npm run build` (default) |
   | Output Directory | `dist` (default) |

3. Expand **Environment Variables** and add (for **Production** and **Preview**):

   | Key                 | Value |
   |---------------------|-------|
   | `VITE_API_BASE_URL` | `https://lsplit.onrender.com/api` |

   Use **your** Render URL from Part 2 step 7. Keep the `/api` suffix, no trailing slash.
   Vite bakes this in at **build time** — if you ever change it, you must redeploy the frontend.

4. Click **Deploy**. You'll get a URL like `https://l-split.vercel.app`.
5. `frontend/vercel.json` already rewrites all routes to `index.html`, so refreshing on
   `/groups/...` or `/events/...` works.

---

## Part 4 — CORS (connecting the two)

Nothing to do for the default setup: the backend allows any `https://*.vercel.app` origin
out of the box.

If you later add a **custom domain** to Vercel, set one extra env var on Render
(no code change needed — added in this round of fixes):

```
CORS_ALLOWED_ORIGINS=https://*.vercel.app,https://yourdomain.com,http://localhost:5173
```

Save it and Render restarts the service automatically.

---

## Part 5 — Verify the deployment

1. Open your Vercel URL.
2. **Register** two accounts (use a private window for the second one).
3. With account A: create a group, add account B by email, create an event, add an
   expense split between both.
4. Check the **Balances** tab shows who owes whom, record a settlement from account B,
   and confirm balances go to zero.
5. Refresh the page mid-session — you should stay logged in (this was one of the bugs fixed).

First request after idle can take up to a minute (Render + Neon cold start) — that's the
free tiers, not a bug.

---

## Part 6 — Automatic redeploys (CI/CD, optional)

Both platforms already auto-deploy on every push to `main`. If you also want the GitHub
Actions workflow (`.github/workflows/ci-cd.yml`) to gate deploys on tests passing:

1. **Render:** service → **Settings → Deploy Hook** → copy the URL.
   Also set **Settings → Build & Deploy → Auto-Deploy = No** (so only the hook triggers it).
2. **Vercel:** project → **Settings → Git → Deploy Hooks** → create one for `main` → copy the URL.
   Disable auto-deploy under **Settings → Git** if you want the hook to be the only trigger.
3. In the GitHub repo: **Settings → Secrets and variables → Actions** → add:
   - `RENDER_DEPLOY_HOOK` = the Render hook URL
   - `VERCEL_DEPLOY_HOOK` = the Vercel hook URL

Now every push to `main` runs backend + frontend tests first and only deploys if they pass.

---

## Local development

```bash
# Backend — reads backend/.env (already configured with your Neon DB):
cd backend && mvn spring-boot:run

# Frontend — Vite dev server proxies /api to localhost:8080:
cd frontend && npm install && npm run dev
# open http://localhost:5173
```

---

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `This connection has been closed` on boot | `DB_URL` contains `channel_binding=require` — remove it; keep only `?sslmode=require`. |
| Connection timeout on first request | Neon compute was asleep. The pool retries automatically; just wait/retry. |
| Frontend calls fail / hit wrong host in production | `VITE_API_BASE_URL` missing or wrong in Vercel → set it and **redeploy** (it's baked in at build time). |
| CORS error in browser console | Origin not allowed → set `CORS_ALLOWED_ORIGINS` on Render (comma-separated, supports `*` patterns). |
| Render build fails | Root Directory must be `backend` and runtime **Docker**. |
| Logged out on every page refresh | Fixed in this round (auth state now restores before routing). Make sure the latest code is deployed. |
| `401 Unauthorized` responses | Token expired (24 h lifetime) — the app now redirects to login automatically. |
| Flyway `validate` error on boot | Schema drift in Neon — inspect the `flyway_schema_history` table; never edit already-applied migration files. |
