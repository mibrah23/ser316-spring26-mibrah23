# Assignment 5 – Refactoring/Metrics Report

---

## Part 1 – Guided Refactoring

### Changes Made

**Fix 1: Unused/Redundant State in `Book`**

The `Book` class contained a `private boolean available` field that was always kept in sync with `availableCopies > 0`. 
This is a textbook case of redundant state — the boolean never held information that couldn't be derived from `availableCopies`. 
Every method that modified `availableCopies` also had to update `available`, creating two places to maintain the same truth. 
The `isAvailable()` method already returned `availableCopies > 0`, making `available` dead weight.

I removed the field entirely and updated all affected methods: the constructor no longer sets `this.available`, `setAvailableCopies()` no longer updates it, `checkout()` and `returnBook()` no longer update it, `resetAvailability()` no longer sets it, and `checkAvailability()` now directly returns `availableCopies > 0` instead of the removed field. 
External behavior is identical — `isAvailable()` and `checkAvailability()` return the same values as before.

**Fix 2: Magic Numbers in Business Logic (`Checkout`)**

The `checkoutBook()` method returned raw floating-point literals scattered throughout its body. 
I extracted all return codes as named private static constants (`STATUS_BOOK_NULL = 2.1`, `STATUS_REFERENCE_ONLY = 5.0`, `STATUS_RENEWAL = 0.1`). 
The method body now reads as self-documenting code. I also unified the existing eligibility error constants (`PATRON_NULL_ERROR`, `SUSPENDED_ERROR`, etc.) so they reference the new status constants, eliminating duplicate literal definitions.

All 25 tests pass after both changes.



---

## Part 2 – Independent Issue Identification

### Issue 1: Duplicate Method `chkSuspended()` in `Patron`

**Problem:** `Patron` had two public methods that returned the same value:
```java
public boolean isAccountSuspended() { return suspended; }
public boolean chkSuspended()       { return this.suspended; }
```
Both methods returned the `suspended` field with no difference in logic.

**Why it matters:** 
Duplicate methods inflate the public API, confuse callers about which one to use, and create a maintenance risk. 
If the suspension logic ever changes, a developer updating one method might not update the other, introducing a silent inconsistency. 
PMD's `UnusedPrivateMethod` and general code-smell detectors flag this pattern.

**What I changed:** 
Removed `chkSuspended()`. 
Any caller that used it should use `isAccountSuspended()`, which has a clearer, idiomatic Java naming convention (`is` prefix for boolean getters). 
No tests referenced `chkSuspended()`, so no test changes were needed.

---

### Issue 2: Overly Complex Boolean Return in `hasBookCheckedOut()` (`Patron`)

**Problem:** The method was written as:
```java
if (bookMap.containsKey(isbn) == true) {
    return true;
} else {
    return false;
}
```
This is a well-known anti-pattern. 
Comparing a boolean to `true` explicitly (`== true`) is redundant, 
and wrapping a boolean expression in `if/else` only to return `true`/`false` adds noise without meaning.

**Why it matters:** 
This pattern decreases readability, increases cyclomatic complexity unnecessarily, and was flagged by both PMD's `SimplifyBooleanReturns` and `SimplifyBooleanExpressions` rules. 
It is also the kind of code that erodes trust in the codebase, 
if simple things are written verbosely, readers spend extra effort parsing the unnecessary structure.

**What I changed:** 
Replaced the entire method body with `return bookMap.containsKey(isbn);`. 
Behavior is identical, the code is shorter, and the two PMD violations it caused are resolved.

---

## Part 3 – Metrics (Before and After)

### Metrics Chosen

I measured two metrics: **static analysis warning count** (via PMD) and **lines of code (LOC) per class**.

I chose static analysis warnings because they provide an objective, tool-reproducible count of code quality problems, directly tied to the refactoring goals. 
PMD flags real patterns (redundant boolean expressions, high complexity) that correlate with maintenance difficulty. 
I chose LOC per class because it is the simplest size metric and helps confirm that refactoring actually simplified the code rather than just rearranging it.

### Results

**PMD Violations (Static Analysis Warnings)**

| Branch | Total Violations |
|--------|-----------------|
| Dev (before) | 5 |
| refactoring (after) | 4 |

Before violations: CyclomaticComplexity in `checkoutBook`, NcssCount in `Main.main`, 
AvoidDuplicateLiterals in `Main.main`, SimplifyBooleanReturns in `hasBookCheckedOut`, 
SimplifyBooleanExpressions in `hasBookCheckedOut`.

After refactoring: 
The two `Patron.hasBookCheckedOut` violations were eliminated. The remaining 4 violations (CyclomaticComplexity in `checkoutBook`, 
NcssCount and AvoidDuplicateLiterals in `Main.main`, and NcssCount for the `Checkout` class) were not targeted by this refactoring.

**Lines of Code per Class**

| Class | Dev (before) | refactoring (after) | Delta |
|-------|-------------|---------------------|-------|
| Book.java | 143 | 136 | −7 |
| Checkout.java | 446 | 460 | +14 |
| Patron.java | 228 | 220 | −8 |
| Main.java | 90 | 90 | 0 |

`Book` and `Patron` shrank by removing unused code. `Checkout` grew slightly 
because adding descriptive constants takes more lines than bare literals — a worthwhile trade-off for readability.

**Analysis:** The refactoring had a measurable positive impact. The two PMD `Patron` violations were fully resolved. 
The total LOC decreased for the classes with removed dead code. 
The slight increase in `Checkout` is expected and acceptable when replacing magic numbers with named constants; 
readability and self-documentation improved even though the line count increased.

---

## Part 4 – Design Analysis: `Checkout` Class Responsibilities

### Does this represent a design issue?

Yes. `Checkout` currently handles book checkout/renewal processing, fine calculation, ISBN validation, patron type comparison, return processing, and inventory querying. 
This is a broad collection of unrelated responsibilities packed into a single class. 
A class that does this many things is harder to understand, test in isolation, and change without risking unintended side effects. 
The class is a clear violation of good design principles, not merely a style problem.

### Which design principles are involved?

The primary principle violated is the **Single Responsibility Principle (SRP)** from SOLID: a class should have only one reason to change. 
`Checkout` would need to change if fine rates change, if ISBN format rules change, if patron type logic changes, or if checkout workflow changes — four distinct reasons. 
The **Open/Closed Principle** is also relevant: adding a new book type or patron type requires modifying `Checkout` directly rather than extending it. 
Additionally, the **High Cohesion** principle from GRASP is violated; the methods inside `Checkout` do not share a tight, unified purpose.

### Risks for maintainability or testing?

A class with many responsibilities is hard to unit test in isolation because tests must construct a full `Checkout` object with all its state (bookList, patrons, history) even when testing something simple like fine calculation. 
If fine calculation logic changes, the developer must search through a large class to find the relevant method. Bug fixes risk inadvertently breaking unrelated functionality. 
The large number of public methods also means a larger contract to maintain.

### How would you redesign the structure?

I would split `Checkout` into focused collaborating classes:

- **`FineCalculator`** — a stateless utility class containing only `calculateFine(int days, BookType type)`. No dependencies on patron or book inventory state.
- **`IsbnValidator`** — a stateless utility class with `isValidISBN(String isbn)`.
- **`CheckoutService`** — the coordinator that handles checkout, return, and renewal workflows. It holds references to the book inventory and patron registry, and delegates to `FineCalculator` when computing fines on return.
- **`LibraryInventory`** — manages the `bookList` map and `countBooksByType`, `addBook`, `getInventory` operations.

This decomposition gives each class one reason to change, makes `FineCalculator` 
and `IsbnValidator` trivially unit-testable, 
and keeps `CheckoutService` focused on workflow coordination. 
The `Transaction` inner class can remain inside `CheckoutService` or become a standalone value object.

---
