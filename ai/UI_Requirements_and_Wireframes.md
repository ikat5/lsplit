28. UI Requirements & Wireframes

The frontend must be responsive.

Use:

- React 18
- React Router
- Bootstrap 5

Do not use Redux.

Use Context API only for authentication state.

---

Navigation Structure

Unauthenticated:

/login
/register

Authenticated:

/dashboard
/groups/:groupId
/events/:eventId

Navbar:

+--------------------------------------------------+
| L-Split | Dashboard | Logout                     |
+--------------------------------------------------+

Visible on all authenticated pages.

---

Login Page

Route:

/login

Layout:

+----------------------------------+
|            L-Split               |
|                                  |
| Email                            |
| [____________________]           |
|                                  |
| Password                         |
| [____________________]           |
|                                  |
| [ Login ]                        |
|                                  |
| Create Account                   |
+----------------------------------+

Behavior:

- Validate required fields
- Show error message on failed login
- Store JWT in localStorage
- Redirect to Dashboard after success

---

Register Page

Route:

/register

Layout:

+----------------------------------+
|          Create Account          |
|                                  |
| Name                             |
| Email                            |
| Phone                            |
| Password                         |
|                                  |
| [ Register ]                     |
+----------------------------------+

Behavior:

- Validate email format
- Validate password length
- Auto-login after successful registration

---

Dashboard Page

Route:

/dashboard

Purpose:

Show all groups where the user is a member.

Layout:

+------------------------------------------------------+
| Dashboard                                             |
|                                                      |
| [ Create Group ]                                     |
|                                                      |
| My Groups                                             |
|                                                      |
| +----------------------+                             |
| | Berlin Trip          |                             |
| | Members: 5           |                             |
| | Balance: +25.50      |                             |
| +----------------------+                             |
|                                                      |
| +----------------------+                             |
| | Office Lunch         |                             |
| | Members: 8           |                             |
| | Balance: -10.00      |                             |
| +----------------------+                             |
+------------------------------------------------------+

Behavior:

- Clicking a group opens Group View
- Show positive balances in green
- Show negative balances in red

---

Create Group Modal

Opened from Dashboard.

Fields:

Group Name
Description

Buttons:

Create
Cancel

Validation:

- Name required
- Description optional

---

Group View Page

Route:

/groups/{groupId}

Purpose:

Display group information, members, balances, events, and settlements.

Layout:

+------------------------------------------------------+
| Berlin Trip                                           |
| Weekend Trip                                          |
|                                                      |
| [ Add Member ] [ Create Event ]                      |
|                                                      |
| Members                                               |
| ---------------------------------------------------- |
| Alice (ADMIN)                                        |
| Bob                                                  |
| Charlie                                              |
|                                                      |
| Current Balances                                      |
| ---------------------------------------------------- |
| Alice     +40.00                                     |
| Bob       -20.00                                     |
| Charlie   -20.00                                     |
|                                                      |
| Events                                                |
| ---------------------------------------------------- |
| Dinner                                               |
| Hotel                                                |
| Taxi                                                 |
+------------------------------------------------------+

Behavior:

- Clicking an event opens Event View
- Admins can add members
- Admins can create events

---

Add Member Modal

Fields:

Email

Buttons:

Add Member
Cancel

Validation:

- Email required
- User must exist

---

Event View Page

Route:

/events/{eventId}

Purpose:

Display all expenses for an event.

Layout:

+------------------------------------------------------+
| Dinner                                                |
| Italian Restaurant                                   |
|                                                      |
| Total Amount: 150.00                                 |
| Expenses: 3                                          |
|                                                      |
| [ Add Expense ]                                      |
|                                                      |
| Expense List                                         |
| ---------------------------------------------------- |
| Dinner Bill          120.00                          |
| Drinks               20.00                           |
| Dessert              10.00                           |
+------------------------------------------------------+

Behavior:

- Show total event spending
- Show expense count
- Allow adding new expenses

---

Add Expense Modal

Fields:

Description
Amount
Paid By

Split Type:
  EQUAL
  EXACT
  PERCENTAGE

Dynamic fields:

EQUAL:

Select Participants

EXACT:

User + Amount rows

PERCENTAGE:

User + Percentage rows

Validation:

- Amount > 0
- Participants required
- EXACT shares must equal amount
- PERCENTAGE shares must equal 100

---

Balances Section

Located inside Group View.

Layout:

+-------------------------------+
| Balances                      |
|-------------------------------|
| Alice      +40.00             |
| Bob        -20.00             |
| Charlie    -20.00             |
+-------------------------------+

Behavior:

Positive:

- User should receive money

Negative:

- User owes money

---

Suggested Settlements

Located below balances.

Layout:

Suggested Payments

Bob      -> Alice     20.00
Charlie  -> Alice     20.00

Button:

Settle

---

Settlement Modal

Fields:

Payee
Amount

Buttons:

Confirm Settlement
Cancel

Behavior:

- Creates settlement record
- Refresh balances immediately

---

Loading States

During API requests:

Bootstrap Spinner

Show spinner on:

- Login
- Register
- Dashboard
- Group View
- Event View
- Expense Creation
- Settlement Creation

---

Error States

401:

Clear JWT
Redirect to Login

403:

Show Bootstrap Alert

404:

Show "Resource Not Found"

500:

Show Generic Error Message

---

Design Requirements

Use Bootstrap 5 components.

Preferred components:

- Navbar
- Card
- Modal
- Form
- Alert
- Table
- Spinner

Avoid:

- Redux
- Material UI
- Tailwind CSS
- Custom component libraries

The UI should prioritize simplicity, readability, and fast development over visual complexity.
