You are my software engineering assistant for a college sprint project.

Context:
Build an "IT Ticket Management System" based strictly on the requirements below. Use Java only, and keep code quality at the level of a final-year college student entering corporate: clean naming, basic design patterns, no over-architecture.

Requirements (do not add new features not mentioned here):
- Roles: User, Agent, Admin, System
- Ticket workflow: Raised -> Open (pool) -> In-Progress (assigned) -> Awaiting Response -> Resolved -> Reopened -> In-Progress -> Resolved
- User stories include: create ticket, auto ticket ID, auto-assign by least load, role-based ticket views + filters, edit open/reopened description, close ticket with confirmation or move to waiting, update status to awaiting with message, add notes visible to user+admin, search/filter by status/date, rating + flag if <2, admin view agent ratings, reassign wrong category to other team, monthly reports resolved vs reopened, escalation if >24h (admin dashboard -> manager/team member), immutable ticket history logs visible to all, RBAC with admin-only role change, change requests (raise/remove/renew) with admin approval + expiry, system shows expiring changes within 15 days, quarterly change report, archive requests older than 1 year, agent implements approved change and notes approval.

Task A — Sprint Planning Output:
1) Convert the above into:
   - Epics
   - User stories (INVEST style)
   - Acceptance criteria for each story
2) Estimate story points using Fibonacci (1,2,3,5,8,13). Keep scope realistic for a college sprint.
3) Split the stories across 8 people (8 devs) with clear responsibilities.
   - Dev1..Dev8 with modules assigned.
   - Mention dependencies between stories.
4) Produce a sprint plan (Sprint 1 only) showing what can be finished first.

Task B — Implementation Output (Java only):
Implement a working backend with the following constraints:
- Tech: Java + Spring Boot (REST APIs). Use Spring Data JPA + H2 (or MySQL) for persistence.
- Authentication: simple login simulation is OK (basic username + role in DB), no OAuth.
- Use layered architecture: controller -> service -> repository -> entity -> dto.
- Validation: basic (null/empty).
- Use enums for ticket status and roles.
- Implement RBAC checks in service layer (simple role checks).
- Ticket assignment logic: assign to the agent with least number of "active" tickets (Open + In-Progress + Awaiting). If tie, pick lowest agentId.
- Ticket history must be immutable: every status change, note, reassign, escalation, change approval/implementation should create a history record.
- Escalation rule: if ticket is not resolved within 24 hours from created time, allow user to escalate; system marks escalated and shows in admin dashboard; admin can forward to manager or reassign to another agent.
- Rating rule: user can rate only after ticket is resolved; if rating < 2, flag agent.
- Change requests: create/renew/remove; requires admin approval; has expiry date; system endpoint shows changes expiring within next 15 days; archive requests older than 1 year to an archive table (can be a scheduled job or manual endpoint for demo).

Deliverables:
1) Data model (Entities) and DB schema details.
2) REST API list with endpoints + request/response JSON examples.
3) Full code skeleton + key implementations:
   - Ticket create, assign, update status, close with confirmation/waiting, reopen, edit description rules
   - Notes
   - Search/filter by status/date range
   - Rating + admin view ratings + low rating flag
   - Reassign to other team
   - Reports: monthly resolved vs reopened
   - Escalation flow
   - History log
   - RBAC
   - Change request flows + expiring list + quarterly report + archive + implementation notes
4) Provide commands to run project.

Output formatting rules:
- First print Sprint Planning (Epics -> Stories -> Points -> Assignments -> Sprint 1 plan)
- Then print Implementation (Architecture -> Entities -> APIs -> Code)
- Keep it concise but complete enough that we can build and demo it.
- Do NOT use any language other than Java for the backend code.
