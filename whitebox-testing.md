# White Box Testing Report - Assignment 3

**Student Name:** [Malek Ibrahim]
**ASU ID:** [mibrah23]
**Date:** [2/4/2026]

---

## Part 1: Control Flow Graph for countBooksByType()

### Graph Description



**Nodes:**
- Node 370: if (type == null) - Decision
- Node 371: return 0 - Exit
- Node 374: int looped = 0 - Statement
- Node 377: for (Book b : bookList.values()) - Loop Decision
- Node 379: if (b == null) - Decision
- Node 380: continue - Statement
- Node 384: if (b.getType() == type) - Decision
- Node 386: if (onlyAvailable) - Decision
- Node 388: if (b.isAvailable()) - Decision
- Node 389: looped++ - Statement
- Node 393: looped++ - Statement
- Node 398: return looped - Exit

**Edges:**
1. 370 -> 371 (TRUE: type is null)
2. 370 -> 374 (FALSE: type is not null)
3. 374 -> 377 (sequence)
4. 377 -> 379 (TRUE: has more books)
5. 377 -> 398 (FALSE: no more books)
6. 379 -> 380 (TRUE: b is null)
7. 379 -> 384 (FALSE: b is not null)
8. 380 -> 377 (continue loop back)
9. 384 -> 386 (TRUE: type matches)
10. 384 -> 377 (FALSE: type doesn't match)
11. 386 -> 388 (TRUE: onlyAvailable is true)
12. 386 -> 393 (FALSE: onlyAvailable is false)
13. 388 -> 389 (TRUE: book is available)
14. 388 -> 377 (FALSE: book not available)
15. 389 -> 377 (loop back after increment)
16. 393 -> 377 (loop back after increment)

**Total Edges:** 16


**Total Nodes:** 12


### Node Coverage Sequences

**Sequence 1: Null Type**
- **Path:** 370 → 371
- **Purpose:** Test null type parameter
- **Test case:** `testCountBooksByType_NullType()`
- **Nodes covered:** 370, 371

**Sequence 2: Empty Book List**
- **Path:** 370 → 374 → 377 → 398
- **Purpose:** Test with no books in inventory
- **Test case:** `testCountBooksByType_EmptyList()`
- **Nodes covered:** 370, 374, 377, 398

**Sequence 3: Null Book in List**
- **Path:** 370 → 374 → 377 → 379 → 380 → 377 → 398
- **Purpose:** Test handling of null book in collection
- **Test case:** `testCountBooksByType_NullBookInList()`
- **Nodes covered:** 379, 380

**Sequence 4: Type Doesn't Match**
- **Path:** 370 → 374 → 377 → 3795 → 384 → 377 → 398
- **Purpose:** Test when book type doesn't match search type
- **Test case:** `testCountBooksByType_TypeNoMatch()`
- **Nodes covered:** 384 (FALSE branch)

**Sequence 5: Count All Books**
- **Path:** 370 → 374 → 377 → 379 → 384 → 386 → 393 → 377 → 398
- **Purpose:** Test counting all books (onlyAvailable=false)
- **Test case:** `testCountBooksByType_CountAll()`
- **Nodes covered:** 384 (TRUE), 386 (FALSE), 393

**Sequence 6: Count Only Available - Book Available**
- **Path:** 370 → 374 → 377 → 379 → 384 → 386 → 388 → 389 → 377 → 398
- **Purpose:** Test counting only available books when book is available
- **Test case:** `testCountBooksByType_OnlyAvailable_BookAvailable()`
- **Nodes covered:** 386 (TRUE), 388 (TRUE), 389

**Sequence 7: Count Only Available - Book Not Available**
- **Path:** 370 → 374 → 377 → 379 → 384 → 386 → 388 → 377 → 398
- **Purpose:** Test counting only available books when book is not available
- **Test case:** `testCountBooksByType_OnlyAvailable_BookNotAvailable()`
- **Nodes covered:** 388 (FALSE)


---

### Edge Coverage Sequences

**The same 7 test sequences above provide complete edge coverage:**

**Test 1:** Covers edges 1, 2
**Test 2:** Covers edges 1, 3, 4, 5
**Test 3:** Covers edges 6, 8
**Test 4:** Covers edge 10
**Test 5:** Covers edges 7, 9, 12, 16
**Test 6:** Covers edges 11, 13, 15
**Test 7:** Covers edge 14


---

## Part 2: Code Coverage with JaCoCo

### Initial Coverage for Checkout.java

**Before adding tests:**
- **Line Coverage:** 51%
- **Branch Coverage:** 46%

### Coverage for countBooksByType()

**Before additional tests:**
- **Branch Coverage:** 100%

**Tests added for calculateFine() (8 tests):**
1. `testCalculateFine_ZeroDays()` - Tests 0 days edge case (early return)
2. `testCalculateFine_NegativeDays()` - Tests negative input (early return)
3. `testCalculateFine_FiveDays_Fiction()` - Tests first pricing tier (1-7 days)
4. `testCalculateFine_TenDays_NonFiction()` - Tests second pricing tier (8-14 days)
5. `testCalculateFine_TwentyDays_Fiction()` - Tests third pricing tier (15+ days)
6. `testCalculateFine_TenDays_Reference()` - Tests double rate for REFERENCE type
7. `testCalculateFine_TwentyDays_Textbook()` - Tests double rate for TEXTBOOK type
8. `testCalculateFine_ExceedsMax()` - Tests maximum fine cap ($25.00)

**Tests added for isValidISBN() (7 tests):**
1. `testIsValidISBN_Null()` - Tests null input handling
2. `testIsValidISBN_Empty()` - Tests empty string handling
3. `testIsValidISBN_Valid10()` - Tests valid ISBN-10 format
4. `testIsValidISBN_Valid13()` - Tests valid ISBN-13 format
5. `testIsValidISBN_WithHyphens()` - Tests hyphen removal and validation
6. `testIsValidISBN_WithLetters()` - Tests rejection of non-digit characters
7. `testIsValidISBN_WrongLength()` - Tests length validation (must be 10 or 13)

**Additional coverage test for checkoutBook():**
1. `testCheckoutBook_FacultyWarning()` - Tests FACULTY patron warning at 18/20 books

### Final Overall Coverage

- **Line Coverage:** 74%
- **Branch Coverage:** 71%

---

## Part 3: checkoutBook() Implementation

### Test-Driven Development Process

**Number of tests from BlackBox assignment:** 27

**Implementation challenges:**
1. Understanding the exact validation order specified in the JavaDoc
2.Correctly implementing the renewal logic

**All tests passing:** [Yes]

---

## Part 4: Reflection

**How did white-box testing differ from black-box testing?**
White Box needs us to know the internal implementation of the code as well as how the system is built. While Black Box focuses on inputs and outputs without knowing the underlying code. 
**Which approach do you find more effective? Why?**
Black Box is better for functionality testing while White Box is better for catching internal logic errors.
**Would you prefer TDD or implementation first test later? Why?**
I feel like TDD is better because it kind of forces you to implement everything correctly.