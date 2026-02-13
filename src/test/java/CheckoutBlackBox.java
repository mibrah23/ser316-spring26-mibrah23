import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Comprehensive Black-Box tests for the Checkout system.
 * Tests all test cases designed in blackbox-testing.md
 */
public class CheckoutBlackBox {

    private Checkout checkout;

    /**
     * Provides the list of Checkout classes to test.
     * Each test will run against ALL implementations.
     */
static Stream<Class<? extends Checkout>> checkoutClassProvider() {
    return Stream.of(
            Checkout.class
            // Checkout0.class,  
            // Checkout1.class,
            // Checkout2.class,
            // Checkout3.class
    );
}

    /**
     * Helper method to create Checkout instance from class using reflection.
     */
    private Checkout createCheckout(Class<? extends Checkout> clazz) throws Exception {
        Constructor<? extends Checkout> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    // PATRON VALIDATION TESTS

    /**
     * T1: Tests null patron handling
     * EP 1.1 - Patron Null State
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T1: Null patron returns error code 3.1")
    public void testNullPatron(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Create valid book
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        checkout.addBook(book);
        
        // Execute with null patron
        double result = checkout.checkoutBook(book, null);
        
        // Verify return code
        assertEquals(3.1, result, 0.01, 
            "Expected error code 3.1 for null patron in " + checkoutClass.getSimpleName());
        
        // Verify no state change
        assertEquals(5, book.getAvailableCopies(), 
            "Book copies should not change for " + checkoutClass.getSimpleName());
    }

    /**
     * T2: Tests null book handling
     * EP 5.1 - Book Null State
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T2: Null book returns error code 2.1")
    public void testNullBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Create valid patron
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        checkout.registerPatron(patron);
        
        // Execute with null book
        double result = checkout.checkoutBook(null, patron);
        
        // Verify return code
        assertEquals(2.1, result, 0.01, 
            "Expected error code 2.1 for null book in " + checkoutClass.getSimpleName());
        
        // Verify patron has no books
        assertEquals(0, patron.getCheckoutCount(), 
            "Patron should have 0 books for " + checkoutClass.getSimpleName());
    }

    /**
     * T3: Tests suspended patron
     * EP 2.1 - Patron Suspension State
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T3: Suspended patron returns error code 3.0")
    public void testSuspendedPatron(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Create book and suspended patron
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setAccountSuspended(true);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(3.0, result, 0.01, 
            "Expected error code 3.0 for suspended patron in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()), 
            "Suspended patron should not have book in " + checkoutClass.getSimpleName());
        assertEquals(5, book.getAvailableCopies(),
            "Book copies should not change in " + checkoutClass.getSimpleName());
    }

    /**
     * T4: Tests patron with exactly 3 overdue books
     * EP 3.3, BVA 1.4 - Overdue Count at Boundary
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T4: Patron with 3 overdue books returns error code 4.0")
    public void testThreeOverdueBooks(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(3); // Exactly at threshold
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(4.0, result, 0.01, 
            "Expected error code 4.0 for 3 overdue books in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have book in " + checkoutClass.getSimpleName());
    }

    /**
     * T5: Tests patron with exactly $10.00 in fines
     * EP 4.2, BVA 2.3 - Fine Balance at Boundary
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T5: Patron with $10.00 fines returns error code 4.1")
    public void testExactlyTenDollarFines(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.addFine(10.00); // Exactly at threshold
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(4.1, result, 0.01, 
            "Expected error code 4.1 for $10.00 fines in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have book in " + checkoutClass.getSimpleName());
    }

    // BOOK VALIDATION TESTS 

    /**
     * T6: Tests reference-only book
     * EP 6.1, EP 10.3 - Reference Book Type
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T6: Reference book returns error code 5.0")
    public void testReferenceOnlyBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: REFERENCE type book
        Book book = new Book("978-0-123-45678-9", "Encyclopedia", 
                             "Author", Book.BookType.REFERENCE, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(5.0, result, 0.01, 
            "Expected error code 5.0 for reference book in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have reference book in " + checkoutClass.getSimpleName());
    }

    /**
     * T7: Tests unavailable book (0 copies)
     * EP 7.1, BVA 6.1 - Book Availability Boundary
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T7: Unavailable book returns error code 2.0")
    public void testUnavailableBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Book with 0 copies
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        book.setAvailableCopies(0); // All checked out
        
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(2.0, result, 0.01, 
            "Expected error code 2.0 for unavailable book in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have book in " + checkoutClass.getSimpleName());
    }

    /**
     * T8: Tests STUDENT patron at max limit (10 books)
     * EP 9.3, BVA 3.5 - Checkout Limit Boundary
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T8: STUDENT at max limit returns error code 3.2")
    public void testStudentAtMaxLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        // Give patron 10 books (STUDENT max)
        for (int i = 1; i <= 10; i++) {
            patron.addCheckedOutBook("ISBN-" + i, LocalDate.now().plusDays(30));
        }
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(3.2, result, 0.01, 
            "Expected error code 3.2 for STUDENT at max limit in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have new book in " + checkoutClass.getSimpleName());
        assertEquals(10, patron.getCheckoutCount(),
            "Checkout count should remain 10 in " + checkoutClass.getSimpleName());
    }

    // SUCCESS TESTS 

    /**
     * T9: Tests successful checkout
     * EP 7.2, EP 9.1 - Normal Success
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T9: Successful checkout returns 0.0 and updates state")
    public void testSuccessfulCheckout(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        // Give patron 2 books (well below max)
        patron.addCheckedOutBook("ISBN-1", LocalDate.now().plusDays(30));
        patron.addCheckedOutBook("ISBN-2", LocalDate.now().plusDays(30));
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify return code
        assertEquals(0.0, result, 0.01, 
            "Expected success code 0.0 in " + checkoutClass.getSimpleName());
        
        // Verify state changes
        assertEquals(4, book.getAvailableCopies(), 
            "Book copies should decrease from 5 to 4 in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book in checked out list in " + checkoutClass.getSimpleName());
        assertEquals(3, patron.getCheckoutCount(),
            "Patron checkout count should be 3 in " + checkoutClass.getSimpleName());
    }

    /**
     * T10: Tests renewal
     * EP 8.1 - Renewal Success
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T10: Renewal returns 0.1 and extends due date")
    public void testRenewal(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        // Patron already has this book checked out
        LocalDate oldDueDate = LocalDate.now().minusDays(5); // Old due date
        patron.addCheckedOutBook(book.getIsbn(), oldDueDate);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        int copiesBefore = book.getAvailableCopies();
        
        // Execute renewal
        double result = checkout.checkoutBook(book, patron);
        
        // Verify return code
        assertEquals(0.1, result, 0.01, 
            "Expected renewal code 0.1 in " + checkoutClass.getSimpleName());
        
        // Verify state changes
        assertEquals(copiesBefore, book.getAvailableCopies(), 
            "Book copies should NOT change for renewal in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should still have book in " + checkoutClass.getSimpleName());
        
        // Verify due date was updated (should be after old due date)
        LocalDate newDueDate = patron.getCheckedOutBooks().get(book.getIsbn());
        assertTrue(newDueDate.isAfter(oldDueDate),
            "Due date should be updated for renewal in " + checkoutClass.getSimpleName());
    }

    /**
     * T11: Tests checkout with 1 overdue book warning
     * EP 3.2, BVA 1.2 - Warning Level
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T11: Patron with 1 overdue returns 1.0")
    public void testOneOverdueWarning(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(1); // 1 overdue book
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed with warning
        assertEquals(1.0, result, 0.01, 
            "Expected warning code 1.0 for 1 overdue in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book despite warning in " + checkoutClass.getSimpleName());
        assertEquals(4, book.getAvailableCopies(),
            "Book should be checked out in " + checkoutClass.getSimpleName());
    }

    /**
     * T12: Tests checkout with 2 overdue books warning
     * EP 3.2, BVA 1.3 - Warning Level High
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T12: Patron with 2 overdue returns 1.0")
    public void testTwoOverdueWarning(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(2); // 2 overdue books
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed with warning
        assertEquals(1.0, result, 0.01, 
            "Expected warning code 1.0 for 2 overdue in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book despite warning in " + checkoutClass.getSimpleName());
    }

    /**
     * T13: Tests STUDENT in warning zone (within 2 of max)
     * EP 9.2, BVA 3.3 - Limit Warning
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T13: STUDENT with 8 books returns 1.1")
    public void testStudentWarningZone(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        // Give patron 8 books (STUDENT max is 10, so within 2)
        for (int i = 1; i <= 8; i++) {
            patron.addCheckedOutBook("ISBN-" + i, LocalDate.now().plusDays(30));
        }
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute - checking out 9th book
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed with warning
        assertEquals(1.1, result, 0.01, 
            "Expected warning code 1.1 for STUDENT with 8 books in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book in " + checkoutClass.getSimpleName());
        assertEquals(9, patron.getCheckoutCount(),
            "Checkout count should be 9 in " + checkoutClass.getSimpleName());
    }

    /**
     * T14: Tests FACULTY in warning zone
     * EP 9.2, BVA 4.3 - FACULTY Limit Warning
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T14: FACULTY with 18 books returns 1.1")
    public void testFacultyWarningZone(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Prof", 
                                   "prof@test.com", Patron.PatronType.FACULTY);
        
        // Give patron 18 books (FACULTY max is 20, so within 2)
        for (int i = 1; i <= 18; i++) {
            patron.addCheckedOutBook("ISBN-" + i, LocalDate.now().plusDays(60));
        }
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute - checking out 19th book
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(1.1, result, 0.01, 
            "Expected warning code 1.1 for FACULTY with 18 books in " + checkoutClass.getSimpleName());
        assertEquals(19, patron.getCheckoutCount(),
            "Checkout count should be 19 in " + checkoutClass.getSimpleName());
    }

    /**
     * T15: Tests CHILD in warning zone
     * EP 9.2, BVA 5.2 - CHILD Limit Warning
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T15: CHILD with 1 book returns 1.1")
    public void testChildWarningZone(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Kids Book", 
                             "Author", Book.BookType.CHILDREN, 5);
        Patron patron = new Patron("P001", "Test Child", 
                                   "parent@test.com", Patron.PatronType.CHILD);
        
        // Give patron 1 book (CHILD max is 3, so within 2)
        patron.addCheckedOutBook("ISBN-1", LocalDate.now().plusDays(14));
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute - checking out 2nd book
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(1.1, result, 0.01, 
            "Expected warning code 1.1 for CHILD with 1 book in " + checkoutClass.getSimpleName());
        assertEquals(2, patron.getCheckoutCount(),
            "Checkout count should be 2 in " + checkoutClass.getSimpleName());
    }

    /**
     * T16: Tests checkout of last available copy
     * BVA 6.2 - Last Copy Boundary
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T16: Last copy checkout makes book unavailable")
    public void testLastCopyCheckout(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Book with only 1 copy
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        book.setAvailableCopies(1); // Only 1 copy left
        
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(0.0, result, 0.01, 
            "Expected success code 0.0 in " + checkoutClass.getSimpleName());
        assertEquals(0, book.getAvailableCopies(),
            "Book should have 0 copies after checkout in " + checkoutClass.getSimpleName());
        assertFalse(book.isAvailable(),
            "Book should be unavailable after last copy in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book in " + checkoutClass.getSimpleName());
    }

    /**
     * T17: Tests fines just below threshold
     * BVA 2.2 - Fines Boundary Below
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T17: Patron with $9.99 fines can checkout")
    public void testFinesJustBelowThreshold(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.addFine(9.99); // Just below $10 threshold
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed
        assertEquals(0.0, result, 0.01, 
            "Expected success code 0.0 for $9.99 fines in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have book in " + checkoutClass.getSimpleName());
    }

    /**
     * T18: Tests fines just above threshold
     * BVA 2.4 - Fines Boundary Above
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T18: Patron with $10.01 fines cannot checkout")
    public void testFinesJustAboveThreshold(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.addFine(10.01); // Just above $10 threshold
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should fail
        assertEquals(4.1, result, 0.01, 
            "Expected error code 4.1 for $10.01 fines in " + checkoutClass.getSimpleName());
        assertFalse(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should not have book in " + checkoutClass.getSimpleName());
    }

    /**
     * T19: Tests 4 overdue books (above threshold)
     * BVA 1.5 - Overdue Boundary Above
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T19: Patron with 4 overdue books returns 4.0")
    public void testFourOverdueBooks(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(4); // Above threshold
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify
        assertEquals(4.0, result, 0.01, 
            "Expected error code 4.0 for 4 overdue in " + checkoutClass.getSimpleName());
    }

    /**
     * T20: Tests circulating FICTION book
     * EP 10.1 - Book Type Valid
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T20: FICTION book can be checked out")
    public void testCirculatingFictionBook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: FICTION book
        Book book = new Book("978-0-123-45678-9", "Fiction Novel", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed
        assertEquals(0.0, result, 0.01, 
            "Expected success code 0.0 for FICTION book in " + checkoutClass.getSimpleName());
        assertTrue(patron.hasBookCheckedOut(book.getIsbn()),
            "Patron should have FICTION book in " + checkoutClass.getSimpleName());
    }

    /**
     * T21: Tests circulating TEXTBOOK
     * EP 10.4 - Book Type Valid
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T21: TEXTBOOK can be checked out")
    public void testCirculatingTextbook(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: TEXTBOOK
        Book book = new Book("978-0-123-45678-9", "Math Textbook", 
                             "Author", Book.BookType.TEXTBOOK, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed
        assertEquals(0.0, result, 0.01, 
            "Expected success code 0.0 for TEXTBOOK in " + checkoutClass.getSimpleName());
    }

    // VALIDATION ORDER TESTS 

    /**
     * T22: Tests validation order - null patron checked first
     * EP 1.1, EP 5.1 - Priority Testing
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T22: Null patron checked before null book")
    public void testValidationOrderNullFirst(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Execute with both null
        double result = checkout.checkoutBook(null, null);
        
        // Verify - patron null (3.1) should be checked first, not book null (2.1)
        assertEquals(3.1, result, 0.01, 
            "Expected 3.1 (patron null) not 2.1 (book null) - patron checked first in " + 
            checkoutClass.getSimpleName());
    }

    /**
     * T23: Tests validation order - suspended before overdue
     * EP 2.1, EP 3.3 - Priority Testing
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T23: Suspended checked before overdue count")
    public void testValidationOrderSuspendedBeforeOverdue(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: patron who is both suspended AND has 3 overdue
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setAccountSuspended(true);
        patron.setOverdueCount(3);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should return 3.0 (suspended) not 4.0 (overdue)
        assertEquals(3.0, result, 0.01, 
            "Expected 3.0 (suspended) not 4.0 (overdue) - suspension checked first in " + 
            checkoutClass.getSimpleName());
    }

    /**
     * T24: Tests validation order - overdue before fines
     * EP 3.3, EP 4.2 - Priority Testing
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T24: Overdue count checked before fine balance")
    public void testValidationOrderOverdueBeforeFines(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: patron with both 3 overdue AND $10 fines
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(3);
        patron.addFine(10.00);
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should return 4.0 (overdue) not 4.1 (fines)
        assertEquals(4.0, result, 0.01, 
            "Expected 4.0 (overdue) not 4.1 (fines) - overdue checked first in " + 
            checkoutClass.getSimpleName());
    }

    /**
     * T25: Tests warning priority - overdue warning over limit warning
     * EP 3.2, EP 9.2 - Warning Priority
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T25: Overdue warning (1.0) takes priority over limit warning (1.1)")
    public void testOverdueWarningPriorityOverLimitWarning(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: STUDENT with 1 overdue + 8 books (both warnings apply)
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.setOverdueCount(1); // Overdue warning
        
        // 8 books already (will be 9, which is warning zone)
        for (int i = 1; i <= 8; i++) {
            patron.addCheckedOutBook("ISBN-" + i, LocalDate.now().plusDays(30));
        }
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should return 1.0 (overdue warning) not 1.1 (limit warning)
        assertEquals(1.0, result, 0.01, 
            "Expected 1.0 (overdue warning) to take priority over 1.1 (limit warning) in " + 
            checkoutClass.getSimpleName());
    }
    //EDGE CASE TESTS 

    /**
     * T26: Tests renewal when book has 0 available copies
     * EP 8.1, EP 7.1 - Renewal Edge Case
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T26: Renewal succeeds even when book has 0 available copies")
    public void testRenewalSkipsAvailabilityCheck(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: Book with 0 copies, but patron already has it
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        book.setAvailableCopies(0); // No copies available
        
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        patron.addCheckedOutBook(book.getIsbn(), LocalDate.now().minusDays(10));
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        // Execute renewal
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed with renewal code, not fail with 2.0
        assertEquals(0.1, result, 0.01, 
            "Expected renewal code 0.1 even with 0 copies in " + checkoutClass.getSimpleName());
        assertEquals(0, book.getAvailableCopies(),
            "Copies should stay 0 for renewal in " + checkoutClass.getSimpleName());
    }

    /**
     * T27: Tests renewal when patron is at max limit
     * EP 8.1, EP 9.3 - Renewal Edge Case
     */
    @ParameterizedTest
    @MethodSource("checkoutClassProvider")
    @DisplayName("T27: Renewal succeeds even when patron is at max limit")
    public void testRenewalAtMaxLimit(Class<? extends Checkout> checkoutClass) throws Exception {
        checkout = createCheckout(checkoutClass);
        
        // Setup: STUDENT with 10 books (at max)
        Book book = new Book("978-0-123-45678-9", "Test Book", 
                             "Author", Book.BookType.FICTION, 5);
        Patron patron = new Patron("P001", "Test Patron", 
                                   "test@test.com", Patron.PatronType.STUDENT);
        
        // Give patron 10 books including this one
        patron.addCheckedOutBook(book.getIsbn(), LocalDate.now().minusDays(5));
        for (int i = 1; i <= 9; i++) {
            patron.addCheckedOutBook("ISBN-" + i, LocalDate.now().plusDays(30));
        }
        
        checkout.addBook(book);
        checkout.registerPatron(patron);
        
        int copiesBefore = book.getAvailableCopies();
        
        // Execute renewal
        double result = checkout.checkoutBook(book, patron);
        
        // Verify - should succeed with renewal, not fail with 3.2
        assertEquals(0.1, result, 0.01, 
            "Expected renewal code 0.1 even at max limit in " + checkoutClass.getSimpleName());
        assertEquals(10, patron.getCheckoutCount(),
            "Checkout count should stay 10 for renewal in " + checkoutClass.getSimpleName());
        assertEquals(copiesBefore, book.getAvailableCopies(),
            "Copies should not change for renewal in " + checkoutClass.getSimpleName());
    }
}
