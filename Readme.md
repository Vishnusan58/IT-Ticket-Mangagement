# IT Ticket Management System (Console Java)

## Sprint Planning (Task A)

### Epics
1. **Ticket Lifecycle & Assignment** – Users raise tickets, system assigns agents, lifecycle transitions, notes, history.
2. **Search, Filters, and Reporting** – Role-based visibility, status/date filters, monthly and escalation reporting.
3. **Feedback & Quality** – Ticket ratings, low-rating flags, agent performance visibility.
4. **Administration & RBAC** – Role management, reassignment, category corrections.
5. **Change Request Management** – Raise/renew/remove/approve changes with expiry, reporting, and archiving.

### User Stories (INVEST) with Acceptance Criteria & Story Points
| ID | Story | Acceptance Criteria | Points |
|---|---|---|---|
| US1 | As a **User**, I want to raise a ticket with title, description, and category so that support can help me. | Ticket gets auto-ID; status starts Raised → Open; requester visible; history logs creation. | 3 |
| US2 | As a **System**, I want to auto-assign the ticket to the least-loaded agent (tie → lowest id) so workload stays balanced. | Assignment happens on creation; respects Open/In-Progress/Awaiting load; history records assignment. | 5 |
| US3 | As a **User/Agent/Admin**, I want role-based ticket views with status/date filters so I only see relevant tickets. | Users see own tickets; agents see assigned; admins see all; filters by status/date return matching items. | 5 |
| US4 | As a **User**, I want to edit my ticket description only when Open or Reopened to keep details correct. | Edit blocked in other states; history logs edit. | 3 |
| US5 | As a **User/Agent**, I want to close a ticket with confirmation or move it to Awaiting Response with a message. | Close sets status Resolved; awaiting adds message; history captures action. | 5 |
| US6 | As an **Agent/Admin**, I want to add notes visible to user and admin so updates are tracked. | Notes store author/time; visible in ticket view; history logs note. | 3 |
| US7 | As a **User**, I want to reopen a ticket so unresolved issues continue. | Reopen moves to Reopened; history entry added. | 2 |
| US8 | As a **User**, I want to rate a resolved ticket so service quality is measured. | Rating only allowed when Resolved; rating <2 flags agent; stored per ticket. | 5 |
| US9 | As an **Admin**, I want to view agent ratings and flags so I can coach low performers. | Report shows average rating and low-rating count per agent. | 3 |
| US10 | As an **Admin**, I want to reassign tickets to correct teams when category is wrong. | Admin selects new agent; history logs reason; assignment changes. | 3 |
| US11 | As an **Admin/System**, I want monthly resolved vs reopened counts and unresolved >24h escalation list. | Report shows counts per month; escalations list non-resolved tickets older than 24h. | 8 |
| US12 | As an **Admin**, I want to manage change requests (approve/renew/remove) with expiry and archive rules. | Only admin approves; renew/remove enforce permissions; archive after 1 year; expiring-within-15-days view. | 8 |
| US13 | As an **Agent**, I want to implement approved changes and log implementation notes. | Only approved changes can be implemented; note stored; status becomes Implemented. | 5 |

### Team Assignment (Dev1–Dev8)
- **Dev1**: US1 (ticket creation basics), US4 (description edit).
- **Dev2**: US2 (auto-assignment logic) – depends on US1.
- **Dev3**: US3 (role-based views & filters) – depends on US1.
- **Dev4**: US5 (close/await), US7 (reopen) – depends on US1.
- **Dev5**: US6 (notes), US8 (rating/flag) – depends on US1/US5.
- **Dev6**: US9 (agent rating report), US11 (reports/escalations) – depends on US8.
- **Dev7**: US10 (reassign/RBAC) – depends on US2/US3.
- **Dev8**: US12 & US13 (change requests lifecycle) – independent of tickets but shares user model.

### Dependencies
- US2 depends on US1 (ticket exists to assign).
- US3 depends on US1 (data to view).
- US5/US7 depend on lifecycle from US1.
- US8 depends on closure from US5.
- US9 depends on ratings from US8.
- US10 depends on tickets and assignment from US2.
- US11 depends on lifecycle data from US5/US7.
- US12/US13 depend on user/admin/agent roles but isolated from ticket flow.

### Sprint 1 Plan (focus on core flow)
- Week 1: US1, US2, US3, US4 (foundational ticket flow & views).
- Week 2: US5, US6, US7 (closure/awaiting, notes, reopen).
- Week 3: US8, US9, US10 (ratings, agent performance, reassignment).
- Week 4: US11, US12, US13 (reports/escalations, change requests lifecycle, implementation notes) and integration polish.

## Implementation (Task B)

### Package Structure
- `com.ittm.model` – Entities/enums (Role, TicketStatus, ChangeRequestStatus, User, Ticket, Note, TicketHistoryEntry, ChangeRequest).
- `com.ittm.repository` – `DataStore` in-memory collections.
- `com.ittm.service` – Business logic (TicketService, ChangeRequestService, UserService).
- `com.ittm.ui` – Console menus and flows (`ConsoleApp`).
- `com.ittm.util` – Helpers (`DateTimeUtil`).

### Class Diagram (textual)
- **User** (id, name, role)
- **Ticket** (id, requester, assignedAgent, category, title, description, status, createdAt, updatedAt, rating, agentFlagged, notes[], history[])
- **Note** (authorId, authorName, message, createdAt)
- **TicketHistoryEntry** (timestamp, action, performedBy)
- **ChangeRequest** (id, requester, title, description, status, expiryDate, archived, implementationNote, createdAt)
- **DataStore** (users{map}, tickets[list], changeRequests[list], archivedChanges[list])
- **Services**: TicketService (create, assign, status updates, notes, rating, search, reports, escalation, history); ChangeRequestService (raise/renew/remove/approve/implement, expiring, quarterly, archive); UserService (create, change role with RBAC).
- **ConsoleApp** ties services, seeds demo users, and renders role menus.

### Menu Flows
- **User**: create ticket → view own tickets → edit description (Open/Reopened) → add note → close/await → reopen → rate resolved → raise change request.
- **Agent**: view assigned → update status (In-Progress/Awaiting/Resolved) → move to awaiting with message → add note → implement approved change.
- **Admin**: view all → reassign ticket (wrong category/team) → reports (monthly resolved vs reopened, expiring changes) → escalations (>24h unresolved) → approve/reject change → view agent ratings/flags → archive old changes.

### Key Service Behaviors
- **Create ticket**: auto-ID, status Raised→Open, append history, auto-assign least-loaded agent (Open/In-Progress/Awaiting; tie → lowest id).
- **Status updates**: enforce RBAC; close with confirmation or move to Awaiting with message; reopen allowed; awaiting uses agent/user actions.
- **Notes**: agent/admin/user (own ticket) can append notes; history is append-only.
- **Search/filter**: by status and date range for relevant role views.
- **Rating & flag**: rating only after Resolved; rating <2 flags agent; admin report shows averages and flag counts.
- **Reassign**: admin-only; logs reason in history.
- **Reports**: monthly resolved vs reopened summary; escalation list for tickets unresolved >24h.
- **History logging**: immutable list per ticket capturing creation, assignment, edits, notes, and status changes.
- **Change requests**: raise/renew/remove; admin approval; expiry tracking (show within 15 days); quarterly listing; archive older than 1 year; agent/admin implements approved change with note.

### Sample Console Run
```
Welcome to IT Ticket Management Console
Select user id to login (1-Alice, 2-Bob, 100-AgentOne, 101-AgentTwo, 900-Admin, 0-exit):
1
User Menu: 1-Create Ticket ...
Title:
Laptop issue
Description:
Won't boot
Category:
Hardware
Ticket created with id 1
User Menu: ... 9-Logout
9
Select user id ...
100
Agent Menu: 1-View Assigned ...
Ticket 1 [OPEN] Laptop issue assigned:AgentOne rating:null
  Notes:
  History:
   * [2025-... Ticket raised by Alice
   * [... Assigned to agent AgentOne by System assignment
...
```

### How to Compile and Run
```bash
javac -d out $(find src -name "*.java")
java -cp out com.ittm.ui.ConsoleApp
```

