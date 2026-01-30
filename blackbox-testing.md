# Black Box Testing Report - Assignment 2

**Student Name:** [Malek Ibrahim]  
**ASU ID:** [mibrah23]  
**Date:** [1/28]

---

## Part 1: Equivalence Partitioning (EP)

Identify equivalence partitions for the `checkoutBook(Book book, Patron patron)` method based on the specification (JavaDoc).

Create **multiple tables**, one per partition category (e.g., book state, patron state, renewal, limits, etc.).

Do **not** put everything into one table.

**Column Explanations:**
- **Partition ID**: Unique identifier (e.g., EP 1.1, EP 2.1)
- **State**: The specific state/value for this partition (e.g., "Unavailable", "Available")
- **Valid/Invalid**: Whether this partition represents valid or invalid input
- **Input Condition**: Precise condition that defines this partition
- **Expected Return**: What return code you expect
- **Expected Behavior**: What should happen

### Example EP Table: Book Availability

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
|--------------|-------|---------------|----------------|-----------------|------------------|
| EP 1.1 | Unavailable (0 copies) | Invalid | availableCopies == 0 AND other conditions allow checkout | 2.0 | No copies to checkout |
| EP 1.2 | Available (1+ copies) | Valid | availableCopies > 0 AND other conditions allow checkout | Success | Book can be checked out |

**Example test cases:** `testBookAvailable()`, `testUnavailableBook()`

---

### EP Table 1: Patron Null State

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 1.1 | Null Patron | Invalid | patron == null | 3.1 | Checkout rejected, no state changes |
| EP 1.2 | Valid Patron | Valid | patron != null | Depends on other factors | Continue validation |

**Example test cases:** `testNullPatron()`

---

### EP Table 2: Patron Suspension State

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 2.1 | Suspended | Invalid | patron.isAccountSuspended() == true | 3.0 | Checkout rejected, no state changes |
| EP 2.2 | Not Suspended | Valid | patron.isAccountSuspended() == false | Depends | Continue validation |

**Example test cases:** `testSuspendedPatron()`

---

### EP Table 3: Patron Overdue Books Count

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 3.1 | No overdue | Valid | patron.getOverdueCount() == 0 | Success (0.0, 0.1, or 1.1) | No overdue warning |
| EP 3.2 | 1-2 overdue | Valid | patron.getOverdueCount() in [1,2] | 1.0 if checkout succeeds | Warning issued, checkout proceeds |
| EP 3.3 | 3+ overdue | Invalid | patron.getOverdueCount() >= 3 | 4.0 | Checkout rejected, no state changes |

**Example test cases:** `testNoOverdue()`, `testOneOverdue()`, `testTwoOverdue()`, `testThreeOverdue()`

---

### EP Table 4: Patron Fine Balance

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 4.1 | Low fines | Valid | patron.getFineBalance() < 10.0 | Depends | Can proceed with checkout |
| EP 4.2 | High fines | Invalid | patron.getFineBalance() >= 10.0 | 4.1 | Checkout rejected, no state changes |

**Example test cases:** `testLowFines()`, `testHighFines()`

---

### EP Table 5: Book Null State

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 5.1 | Null Book | Invalid | book == null | 2.1 | Checkout rejected, no state changes |
| EP 5.2 | Valid Book | Valid | book != null | Depends | Continue validation |

**Example test cases:** `testNullBook()`

---

### EP Table 6: Book Reference-Only Status

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior ||
| EP 6.1 | Reference-only | Invalid | book.isReferenceOnly() == true | 5.0 | Cannot checkout reference books |
| EP 6.2 | Circulating | Valid | book.isReferenceOnly() == false | Depends | Can be checked out |

**Example test cases:** `testReferenceOnlyBook()`, `testCirculatingBook()`

---

### EP Table 7: Book Availability

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 7.1 | Unavailable (0 copies) | Invalid | book.getAvailableCopies() == 0 AND not renewal | 2.0 | No copies available |
| EP 7.2 | Available (1+ copies) | Valid | book.getAvailableCopies() > 0 | Success (0.0, 1.0, or 1.1) | Book can be checked out, copies decrease |

**Example test cases:** `testUnavailableBook()`, `testAvailableBook()`

---

### EP Table 8: Renewal vs New Checkout

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 8.1 | Renewal | Valid | patron.hasBookCheckedOut(isbn) == true | 0.1 | Due date extended, no copy decrease |
| EP 8.2 | New checkout | Valid | patron.hasBookCheckedOut(isbn) == false | 0.0, 1.0, or 1.1 | Normal checkout, copies decrease |

**Example test cases:** `testRenewal()`, `testNewCheckout()`

---

### EP Table 9: Patron Checkout Count vs Max Limit

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 9.1 | Well below max | Valid | checkoutCount < (maxLimit - 2) | 0.0 or 1.0 | Normal checkout |
| EP 9.2 | Warning zone | Valid | checkoutCount in [maxLimit-2, maxLimit-1] | 1.1 (if no overdue) | Checkout succeeds with warning |
| EP 9.3 | At max limit | Invalid | checkoutCount == maxLimit | 3.2 | Checkout rejected, no state changes |

**Example test cases:** `testBelowMax()`, `testWarningZone()`, `testAtMaxLimit()`

---

### EP Table 10: Book Type

| Partition ID | State | Valid/Invalid | Input Condition | Expected Return | Expected Behavior |
| EP 10.1 | FICTION | Valid | book.getType() == BookType.FICTION | Success | Normal circulation |
| EP 10.2 | NONFICTION | Valid | book.getType() == BookType.NONFICTION | Success | Normal circulation |
| EP 10.3 | REFERENCE | Invalid | book.getType() == BookType.REFERENCE | 5.0 | Cannot checkout |
| EP 10.4 | TEXTBOOK | Valid | book.getType() == BookType.TEXTBOOK | Success | Normal circulation |
| EP 10.5 | CHILDREN | Valid | book.getType() == BookType.CHILDREN | Success | Normal circulation |

**Example test cases:** `testFictionBook()`, `testReferenceBook()`

---

## Part 2: Boundary Value Analysis (BVA)

Important BVA cases may overlap with EP. That is OK. You can reference all relevant EP/BVA coverage in Part 3.

### Example BVA Table: Overdue Count (Threshold: 3)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 1.1 | Below | overdueCount = 0 | Success (depends on other setup) | Below warning threshold |
| BVA 1.2 | Warning High | overdueCount = 2 | 1.0 | Just below reject threshold |
| BVA 1.3 | At | overdueCount = 3 | 4.0 | At rejection boundary |
| BVA 1.4 | Above | overdueCount = 4 | 4.0 | Above rejection boundary |

---

### BVA Table 1: Overdue Count Boundaries (Threshold: 3)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 1.1 | below | overdueCount = 0 | Success (0.0) | No overdue books |
| BVA 1.2 | Warning start | overdueCount = 1 | 1.0 | First warning level |
| BVA 1.3 | Warning high | overdueCount = 2 | 1.0 | Just below reject threshold |
| BVA 1.4 | At | overdueCount = 3 | 4.0 | At rejection threshold |
| BVA 1.5 | Above | overdueCount = 4 | 4.0 | Above rejection threshold |

---

### BVA Table 2: Fine Balance Boundaries (Threshold: $10.00)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 2.1 | below | fineBalance = $0.00 | Success | No fines |
| BVA 2.2 | Just below | fineBalance = $9.99 | Success | Under $10 threshold |
| BVA 2.3 | At | fineBalance = $10.00 | 4.1 | At rejection threshold |
| BVA 2.4 | Just above | fineBalance = $10.01 | 4.1 | Above rejection threshold |
| BVA 2.5 | above | fineBalance = $25.00 | 4.1 | Well over threshold |

---

### BVA Table 3: Checkout Limit Boundaries - STUDENT (Max: 10)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 3.1 | below | checkoutCount = 5 | 0.0 | Safe zone |
| BVA 3.2 | Before warning | checkoutCount = 7 | 0.0 | Last non-warning |
| BVA 3.3 | Warning start | checkoutCount = 8 | 1.1 | Within 2 of max (10-8=2) |
| BVA 3.4 | Warning high | checkoutCount = 9 | 1.1 | Within 1 of max |
| BVA 3.5 | At | checkoutCount = 10 | 3.2 | At limit, rejected |

---

### BVA Table 4: Checkout Limit Boundaries - FACULTY (Max: 20)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 4.1 | below | checkoutCount = 10 | 0.0 | Safe zone |
| BVA 4.2 | Before warning | checkoutCount = 17 | 0.0 | Last non-warning |
| BVA 4.3 | Warning start | checkoutCount = 18 | 1.1 | Within 2 of max (20-18=2) |
| BVA 4.4 | Warning high | checkoutCount = 19 | 1.1 | Within 1 of max |
| BVA 4.5 | At | checkoutCount = 20 | 3.2 | At limit, rejected |

---

### BVA Table 5: Checkout Limit Boundaries - CHILD (Max: 3)

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 5.1 | below | checkoutCount = 0 | 0.0 | No books yet |
| BVA 5.2 | Warning start | checkoutCount = 1 | 1.1 | Within 2 of max (3-1=2) |
| BVA 5.3 | Warning high | checkoutCount = 2 | 1.1 | Within 1 of max |
| BVA 5.4 | At | checkoutCount = 3 | 3.2 | At limit, rejected |

---

### BVA Table 6: Book Available Copies Boundaries

| Test ID | Boundary | Input Value | Expected Return | Rationale |
|---------|----------|-------------|-----------------|-----------|
| BVA 6.1 | Unavailable | availableCopies = 0 | 2.0 | No copies available |
| BVA 6.2 | Last copy | availableCopies = 1 | 0.0 | One copy left (becomes 0) |
| BVA 6.3 | Multiple copies | availableCopies = 2 | 0.0 | Multiple available |
| BVA 6.4 | Many copies | availableCopies = 10 | 0.0 | Many available |

---

## Part 3: Test Cases Designed

List at least **20** test cases you designed based on your EP/BVA analysis.

Each test case should include:
- EP/BVA coverage
- specific inputs / setup
- expected return code
- expected **observable state changes** (if any)

> Do not test console output.

### Test Case Table
At least some of your tests should verify observable state changes, not just return values.

**Checkout0-3 Columns:** Mark each implementation as Pass (✓) or Fail (✗) for this test case. This helps you track which implementations have bugs and will be useful for Part 4 analysis.

| Test ID Name | EP/BVA | Input Description | Expected Return | Expected State Changes | Checkout0 | Checkout1 | Checkout2 | Checkout3 |
|--------------|--------|-------------------|-----------------|------------------------|-----------|-----------|-----------|-----------|
| T1 testNullPatron | EP 1.1 | Null patron, valid available book | 3.1 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T2 testNullBook | EP 5.1 | Null book, valid patron | 2.1 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T3 testSuspendedPatron | EP 2.1 | Suspended patron, valid available book | 3.0 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T4 testThreeOverdueBooks | EP 3.3, BVA 1.4 | Patron with exactly 3 overdue books | 4.0 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T5 testExactlyTenDollarFines | EP 4.2, BVA 2.3 | Patron with exactly $10.00 in fines | 4.1 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T6 testReferenceOnlyBook | EP 6.1, EP 10.3 | Reference book (REFERENCE type), eligible patron | 5.0 | No state changes | ✗ | ✓ | ✓ | ✓ |
| T7 testUnavailableBook | EP 7.1, BVA 6.1 | Book with 0 available copies, eligible patron | 2.0 | No state changes | ✓ | ✓ | ✗ | ✓ |
| T8 testStudentAtMaxLimit | EP 9.3, BVA 3.5 | STUDENT patron with 10 books already checked out | 3.2 | No state changes | ✓ | ✗ | ✗ | ✓ |
| T9 testSuccessfulCheckout | EP 7.2, EP 9.1 | Available book (5 copies), eligible STUDENT patron (2 books), no warnings | 0.0 | Book copies: 5→4; Patron has book in map; checkout count: 2→3 | ✗ | ✗ | ✓ | ✓ |
| T10 testRenewal | EP 8.1 | Patron already has this book checked out | 0.1 | Due date updated; book copies unchanged; checkout count unchanged | ✓ | ✓ | ✗ | ✗ |
| T11 testOneOverdueWarning | EP 3.2, BVA 1.2 | Available book, patron with 1 overdue book | 1.0 | Book checked out with warning; state changes like T9 | ✗ | ✗ | ✓ | ✓ |
| T12 testTwoOverdueWarning | EP 3.2, BVA 1.3 | Available book, patron with 2 overdue books | 1.0 | Book checked out with warning; state changes like T9 | ✓ | ✗ | ✓ | ✗ |
| T13 testStudentWarningZone | EP 9.2, BVA 3.3 | STUDENT patron with 8 books, checking out 9th (within 2 of max) | 1.1 | Book checked out with warning; state changes occur | ✓ | ✗ | ✓ | ✓ |
| T14 testFacultyWarningZone | EP 9.2, BVA 4.3 | FACULTY patron with 18 books, checking out 19th (within 2 of max) | 1.1 | Book checked out with warning; state changes occur | ✓ | ✗ | ✓ | ✓ |
| T15 testChildWarningZone | EP 9.2, BVA 5.2 | CHILD patron with 1 book, checking out 2nd (within 2 of max) | 1.1 | Book checked out with warning; state changes occur | ✓ | ✗ | ✓ | ✓ |
| T16 testLastCopyCheckout | BVA 6.2 | Book with exactly 1 copy available | 0.0 | Book copies: 1→0; book becomes unavailable; patron gets book | ✗ | ✗ | ✓ | ✓ |
| T17 testFinesJustBelowThreshold | BVA 2.2 | Patron with $9.99 in fines, available book | 0.0 | Successful checkout; state changes occur | ✓ | ✗ | ✓ | ✓ |
| T18 testFinesJustAboveThreshold | BVA 2.4 | Patron with $10.01 in fines | 4.1 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T19 testFourOverdueBooks | BVA 1.5 | Patron with 4 overdue books | 4.0 | No state changes | ✓ | ✓ | ✓ | ✓ |
| T20 testCirculatingFictionBook | EP 10.1 | FICTION book (circulating), eligible patron | 0.0 | Normal successful checkout | ✓ | ✗ | ✓ | ✓ |
| T21 testCirculatingTextbook | EP 10.4 | TEXTBOOK book (circulating), eligible patron | 0.0 | Normal successful checkout | ✓ | ✓ | ✓ | ✓ |
| T22 testValidationOrderNullFirst | EP 1.1, EP 5.1 | Both null patron AND null book | 3.1 | Patron null checked first (higher priority) | ✓ | ✗ | ✓ | ✓ |
| T23 testValidationOrderSuspendedBeforeOverdue | EP 2.1, EP 3.3 | Suspended patron with 3 overdue books | 3.0 | Suspension checked first | ✓ | ✓ | ✓ | ✓ |
| T24 testValidationOrderOverdueBeforeFines | EP 3.3, EP 4.2 | Patron with 3 overdue AND $10 fines | 4.0 | Overdue checked before fines | ✓ | ✓ | ✓ | ✓ |
| T25 testOverdueWarningPriorityOverLimitWarning | EP 3.2, EP 9.2 | STUDENT with 1 overdue + 8 books (both warnings apply) | 1.0 | Overdue warning takes priority over limit warning | ✓ | ✓ | ✓ | ✗ |
| T26 testRenewalSkipsAvailabilityCheck | EP 8.1, EP 7.1 | Renewal when book has 0 available copies (patron already has it) | 0.1 | Renewal succeeds; copies stay 0; due date updated | ✗ | ✗ | ✗ | ✗ |
| T27 testRenewalAtMaxLimit | EP 8.1, EP 9.3 | STUDENT renewal when already at 10 books | 0.1 | Renewal succeeds (doesn't count toward new checkout); no copy change | ✓ | ✓ | ✗ | ✗ |
(Total: 27 test cases covering all major scenarios, boundaries, and edge cases)

---

## Part 4: Bug Analysis

### Easter Eggs Found
List any easter egg messages you observed:
- [EASTER EGG #10]: "Testing can show the presence of bugs, but never their absence" - Dijkstra (appeared in multiple tests)
- [EASTER EGG #13]: "Limits exist to be thoroughly tested" (T8)
- [EASTER EGG #14]: "Renew, reuse, recycle... books" (T10)
- [EASTER EGG #15.1]: https://www.youtube.com/watch?v=xvFZjo5PgG0 (T10 - complete renewal testing)
- [EASTER EGG #17]: "The happy path matters too" (multiple Checkout1 tests)
- [EASTER EGG #18]: "Null checking: because null pointer exceptions are not fun" (T2, T22)
- [EASTER EGG #19]: "Availability testing finds the books that aren't there" (T7)
- [EASTER EGG #20]: "Reference books are meant to be consulted, not carried home" (T6)

### Implementation Results

| Implementation | Bugs Found (count) |
|----------------|---------------------|
| Checkout0      | 3 |
| Checkout1      | 4 |
| Checkout2      | 3 |
| Checkout3      | 5 |

### Bugs Discovered
List distinct bugs you identified for each implementation. Each bug must cite at least one test case that revealed it.

**Checkout0:**
- Bug 1: Returns 2.0 (unavailable) for reference books instead of 5.0 (reference-only)
  - Revealed by: T6
  - Severity: Medium - Wrong error code but checkout still blocked

- Bug 2: Never updates state for successful checkouts - Returns correct codes but doesn't decrease book copies or add books to patron
  - Revealed by: T9, T11, T16
  - Severity: Critical - Checkouts appear successful but don't actually occur

- Bug 3: Returns 2.0 (unavailable) for renewal attempts when book has 0 copies, should return 0.1 (renewal success)
  - Revealed by: T26
  - Severity: High - Prevents renewals when books are fully checked out

**Checkout1:**
- Bug 1: Completely fails to process successful checkouts - Returns correct codes but never adds books to patron's checked-out list
  - Revealed by: T9, T11, T12, T13, T14, T15, T16, T17, T20
  - Severity: Critical - No checkouts actually complete despite success codes

- Bug 2: Returns 1.1 (warning) instead of 3.2 (rejected) when patron is at maximum limit
  - Revealed by: T8
  - Severity: High - Allows checkouts beyond maximum

- Bug 3: Incorrect validation order - Checks book null before patron null
  - Revealed by: T22
  - Severity: Medium - Violates specification validation order

- Bug 4: Returns 2.0 (unavailable) for renewal with 0 copies instead of 0.1 (renewal success)
  - Revealed by: T26
  - Severity: High - Blocks legitimate renewals
**Checkout2:**
- Bug 1: Doesn't check book availability for non-renewals - Returns 0.0 for unavailable books
  - Revealed by: T7
  - Severity: High - Allows checking out unavailable books

- Bug 2: Returns 1.1 (warning) instead of 3.2 (rejected) when at max limit
  - Revealed by: T8
  - Severity: High - Allows exceeding checkout limits

- Bug 3: Doesn't distinguish renewals - Returns 0.0 instead of 0.1 for all renewals
  - Revealed by: T10, T26, T27
  - Severity: Low - Functional but wrong return codes
**Checkout3:**
- Bug 1: Renewal incorrectly decreases available copies - Should not change but does (5→4)
  - Revealed by: T10
  - Severity: High - Causes inventory tracking errors

- Bug 2: Returns 0.0 instead of 1.0 for 2 overdue books warning
  - Revealed by: T12
  - Severity: Low - Missing warning but checkout completes

- Bug 3: Returns 1.1 (limit warning) instead of 1.0 (overdue warning) when both apply  
  - Revealed by: T25
  - Severity: Low - Wrong priority but checkout succeeds

- Bug 4: Returns 2.0 (unavailable) for renewal with 0 copies instead of 0.1
  - Revealed by: T26
  - Severity: High - Blocks legitimate renewals

- Bug 5: Returns 3.2 (at max) for renewal at max limit instead of 0.1 (renewal success)
  - Revealed by: T27
  - Severity: High - Prevents renewals when at limit

### Comparative Analysis
Compare the four implementations:
- Which bugs are most critical (cause the worst failures)?
- Which implementation would you use if you had to choose?
- Why? Justify your choice considering bug severity and frequency.

The most critical bugs are in **Checkout1**, which has 12 test failures and completely fails to process any successful checkouts. 

**Checkout0** and **Checkout3** each have 5 failures. 

**Checkout2** also has 5 failures with the most severe being that it doesn't check availability and allows exceeding limits.

**However, none of the implementations handle renewals correctly**, especially edge cases (T26, T27). This is a major flaw across all implementations.

I would select **Checkout2** as the least problematic. While it has critical bugs (not checking availability, wrong max limit handling), at least successful checkouts actually update state correctly and most core functionality works. 

## Part 5: Reflection

**Which testing technique was most effective for finding bugs?**
State verification testing was most effective. Many implementations (Checkout0, Checkout1) 
returned correct success codes but failed to update state.
**What was the most challenging aspect of this assignment?**
Designing the test cases to cover all cases and validation order scenarios, it also took alot more time than i expected.
**How did you decide on your EP and BVA?**
The EP is derived from the JavaDoc specifications, while For BVA i looked for every numeric threshold in the spec and created test values just below, at, and above each boundary.

**Describe one test where checking only the return value would NOT have been sufficient to detect a bug.**
Test T9 (successful checkout). Checkout0 and Checkout1 both returned the correct code 0.0 (success), but when I verified the state changes, they failed.
