import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Sample White-Box tests for the Checkout system.
 * This class demonstrates how to write white-box tests using:
 * - Control Flow Graph (CFG) analysis
 * - Statement coverage
 * - Branch coverage
 * - Path coverage
 *
 * White-box testing focuses on testing the IMPLEMENTATION by
 * examining the code structure and ensuring all paths are tested.
 */
public class CheckoutWhiteBoxSample {

    private Checkout checkout;

    @BeforeEach
    public void setUp() {
        checkout = new Checkout();
    }

    /**
     * Test Sequence 1: Covers nodes 1, 2
     * Tests null type branch
     */
    @Test
    @DisplayName("WB Test 1: countBooksByType - null type")
    public void testCountBooksByType_NullType() {
        // Execute: type is null
        int result = checkout.countBooksByType(null, false);
        
        // Verify: returns 0
        assertEquals(0, result, "Should return 0 for null type");
    }

    /**
     * Test Sequence 2: Covers nodes 1, 3, 4, 12
     * Tests empty book list (loop never executes)
     */
    @Test
    @DisplayName("WB Test 2: countBooksByType - empty book list")
    public void testCountBooksByType_EmptyList() {
        // Setup: bookList is already empty
        
        // Execute
        int result = checkout.countBooksByType(Book.BookType.FICTION, false);
        
        // Verify
        assertEquals(0, result, "Should return 0 for empty book list");
    }

    /**
     * Test Sequence 3: Covers nodes 5, 6
     * Tests null book in collection (continue statement)
     */
 /*    @Test
    @DisplayName("WB Test 3: countBooksByType - null book in list")
    public void testCountBooksByType_NullBookInList() {
        // Setup: add null to book list
        checkout.getInventory().put("null-entry", null);
        
        // Execute
        int result = checkout.countBooksByType(Book.BookType.FICTION, false);
        
        // Verify: null book is skipped
        assertEquals(0, result, "Should skip null books and return 0");
    }
        */

    /**
     * Test Sequence 4: Covers node 7 (FALSE branch)
     * Tests when book type doesn't match
     */
    @Test
    @DisplayName("WB Test 4: countBooksByType - type doesn't match")
    public void testCountBooksByType_TypeNoMatch() {
        // Setup: add FICTION book
        Book book = new Book("978-0-123-45678-9", "Fiction Book", 
                             "Author", Book.BookType.FICTION, 5);
        checkout.addBook(book);
        
        // Execute: search for TEXTBOOK (doesn't match)
        int result = checkout.countBooksByType(Book.BookType.TEXTBOOK, false);
        
        // Verify: no matches found
        assertEquals(0, result, "Should return 0 when no books match type");
    }

    /**
     * Test Sequence 5: Covers nodes 7 (TRUE), 8 (FALSE), 11
     * Tests counting all books (onlyAvailable = false)
     */
    @Test
    @DisplayName("WB Test 5: countBooksByType - count all books")
    public void testCountBooksByType_CountAll() {
        // Setup: add FICTION book
        Book book = new Book("978-0-123-45678-9", "Fiction Book", 
                             "Author", Book.BookType.FICTION, 5);
        checkout.addBook(book);
        
        // Execute: onlyAvailable = false (count all)
        int result = checkout.countBooksByType(Book.BookType.FICTION, false);
        
        // Verify: counts the book
        assertEquals(1, result, "Should count book when onlyAvailable=false");
    }

    /**
     * Test Sequence 6: Covers nodes 8 (TRUE), 9 (TRUE), 10
     * Tests counting only available books when book IS available
     */
    @Test
    @DisplayName("WB Test 6: countBooksByType - only available, book available")
    public void testCountBooksByType_OnlyAvailable_BookAvailable() {
        // Setup: add available FICTION book
        Book book = new Book("978-0-123-45678-9", "Fiction Book", 
                             "Author", Book.BookType.FICTION, 5);
        checkout.addBook(book);
        // book.isAvailable() returns true (has 5 copies)
        
        // Execute: onlyAvailable = true
        int result = checkout.countBooksByType(Book.BookType.FICTION, true);
        
        // Verify: counts the available book
        assertEquals(1, result, "Should count available book when onlyAvailable=true");
    }

    /**
     * Test Sequence 7: Covers node 9 (FALSE branch)
     * Tests counting only available books when book is NOT available
     */
    @Test
    @DisplayName("WB Test 7: countBooksByType - only available, book not available")
    public void testCountBooksByType_OnlyAvailable_BookNotAvailable() {
        // Setup: add unavailable FICTION book
        Book book = new Book("978-0-123-45678-9", "Fiction Book", 
                             "Author", Book.BookType.FICTION, 5);
        book.setAvailableCopies(0); // make unavailable
        checkout.addBook(book);
        
        // Execute: onlyAvailable = true
        int result = checkout.countBooksByType(Book.BookType.FICTION, true);
        
        // Verify: doesn't count unavailable book
        assertEquals(0, result, "Should not count unavailable book when onlyAvailable=true");
    }

    /**
 * Test: Loop runs multiple times
 * This ensures the loop-back edges are properly tested
 */
@Test
@DisplayName("WB Test 8: countBooksByType - multiple books of same type")
public void testCountBooksByType_MultipleMatchingBooks() {
    // Setup: add 3 FICTION books with different availability
    Book book1 = new Book("ISBN1", "Book1", "Author", Book.BookType.FICTION, 5);
    Book book2 = new Book("ISBN2", "Book2", "Author", Book.BookType.FICTION, 3);
    Book book3 = new Book("ISBN3", "Book3", "Author", Book.BookType.FICTION, 0);
    
    checkout.addBook(book1);
    checkout.addBook(book2);
    checkout.addBook(book3);
    
    // Execute: count all FICTION (onlyAvailable = false)
    int result = checkout.countBooksByType(Book.BookType.FICTION, false);
    
    // Verify: counts all 3
    assertEquals(3, result, "Should count all 3 FICTION books");
}

/**
 * Test: Multiple books with mixed availability
 */
@Test
@DisplayName("WB Test 9: countBooksByType - mixed availability")
public void testCountBooksByType_MixedAvailability() {
    // Setup: add 2 available, 1 unavailable FICTION book
    Book book1 = new Book("ISBN1", "Available1", "Author", Book.BookType.FICTION, 5);
    Book book2 = new Book("ISBN2", "Available2", "Author", Book.BookType.FICTION, 3);
    Book book3 = new Book("ISBN3", "Unavailable", "Author", Book.BookType.FICTION, 5);
    book3.setAvailableCopies(0); // Make unavailable
    
    checkout.addBook(book1);
    checkout.addBook(book2);
    checkout.addBook(book3);
    
    // Execute: count only available FICTION
    int result = checkout.countBooksByType(Book.BookType.FICTION, true);
    
    // Verify: counts only 2 available
    assertEquals(2, result, "Should count only 2 available FICTION books");
}

/**
 * Test: Multiple iterations with different types
 */
@Test
@DisplayName("WB Test 10: countBooksByType - mixed types in list")
public void testCountBooksByType_MixedTypes() {
    // Setup: add books of different types
    Book fiction1 = new Book("ISBN1", "Fiction1", "Author", Book.BookType.FICTION, 5);
    Book fiction2 = new Book("ISBN2", "Fiction2", "Author", Book.BookType.FICTION, 3);
    Book textbook = new Book("ISBN3", "Textbook", "Author", Book.BookType.TEXTBOOK, 5);
    Book reference = new Book("ISBN4", "Reference", "Author", Book.BookType.REFERENCE, 1);
    
    checkout.addBook(fiction1);
    checkout.addBook(fiction2);
    checkout.addBook(textbook);
    checkout.addBook(reference);
    
    // Execute: count only FICTION
    int result = checkout.countBooksByType(Book.BookType.FICTION, false);
    
    // Verify: finds only 2 FICTION books
    assertEquals(2, result, "Should count only FICTION books, not other types");
}

/**
 * Test: All books unavailable with onlyAvailable=true
 */
@Test
@DisplayName("WB Test 11: countBooksByType - all unavailable with filter")
public void testCountBooksByType_AllUnavailableWithFilter() {
    // Setup: add FICTION books, all unavailable
    Book book1 = new Book("ISBN1", "Book1", "Author", Book.BookType.FICTION, 5);
    Book book2 = new Book("ISBN2", "Book2", "Author", Book.BookType.FICTION, 5);
    book1.setAvailableCopies(0);
    book2.setAvailableCopies(0);
    
    checkout.addBook(book1);
    checkout.addBook(book2);
    
    // Execute: count only available
    int result = checkout.countBooksByType(Book.BookType.FICTION, true);
    
    // Verify: finds none
    assertEquals(0, result, "Should return 0 when all books are unavailable");
}

/**
 * Test: Null book mixed with valid books
 */
/**@Test
@DisplayName("WB Test 12: countBooksByType - null and valid books mixed")
public void testCountBooksByType_NullAndValidMixed() {
    // Setup: add null and valid books
    checkout.getInventory().put("null-entry", null);
    Book book = new Book("ISBN1", "Book", "Author", Book.BookType.FICTION, 5);
    checkout.addBook(book);
    
    // Execute
    int result = checkout.countBooksByType(Book.BookType.FICTION, false);
    
    // Verify: skips null, counts valid
    assertEquals(1, result, "Should skip null book and count valid one");
}*/

// CALCULATEFINE() TESTS 

/**
 * Test calculateFine: 0 days overdue
 * Covers: early return branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 0 days")
public void testCalculateFine_ZeroDays() {
    double fine = checkout.calculateFine(0, Book.BookType.FICTION);
    assertEquals(0.0, fine, 0.01, "0 days should have no fine");
}

/**
 * Test calculateFine: negative days
 * Covers: early return branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - negative days")
public void testCalculateFine_NegativeDays() {
    double fine = checkout.calculateFine(-5, Book.BookType.FICTION);
    assertEquals(0.0, fine, 0.01, "Negative days should have no fine");
}

/**
 * Test calculateFine: 1-7 days (first tier)
 * Covers: first pricing tier branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 5 days FICTION")
public void testCalculateFine_FiveDays_Fiction() {
    // 5 days * $0.25 = $1.25
    double fine = checkout.calculateFine(5, Book.BookType.FICTION);
    assertEquals(1.25, fine, 0.01, "5 days FICTION should be $1.25");
}

/**
 * Test calculateFine: 8-14 days (second tier)
 * Covers: second pricing tier branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 10 days NONFICTION")
public void testCalculateFine_TenDays_NonFiction() {
    // (7 * $0.25) + (3 * $0.50) = $1.75 + $1.50 = $3.25
    double fine = checkout.calculateFine(10, Book.BookType.NONFICTION);
    assertEquals(3.25, fine, 0.01, "10 days NONFICTION should be $3.25");
}

/**
 * Test calculateFine: 15+ days (third tier)
 * Covers: third pricing tier branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 20 days FICTION")
public void testCalculateFine_TwentyDays_Fiction() {
    // (7 * $0.25) + (7 * $0.50) + (6 * $1.00) = $1.75 + $3.50 + $6.00 = $11.25
    double fine = checkout.calculateFine(20, Book.BookType.FICTION);
    assertEquals(11.25, fine, 0.01, "20 days FICTION should be $11.25");
}

/**
 * Test calculateFine: REFERENCE type (double rate)
 * Covers: special book type branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 10 days REFERENCE")
public void testCalculateFine_TenDays_Reference() {
    // Normal: (7 * $0.25) + (3 * $0.50) = $3.25
    // Doubled: $3.25 * 2 = $6.50
    double fine = checkout.calculateFine(10, Book.BookType.REFERENCE);
    assertEquals(6.50, fine, 0.01, "10 days REFERENCE should be doubled to $6.50");
}

/**
 * Test calculateFine: TEXTBOOK type (double rate)
 * Covers: special book type branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - 20 days TEXTBOOK")
public void testCalculateFine_TwentyDays_Textbook() {
    // Normal: (7 * $0.25) + (7 * $0.50) + (6 * $1.00) = $11.25
    // Doubled: $11.25 * 2 = $22.50
    double fine = checkout.calculateFine(20, Book.BookType.TEXTBOOK);
    assertEquals(22.50, fine, 0.01, "20 days TEXTBOOK should be doubled to $22.50");
}

/**
 * Test calculateFine: exceeds maximum
 * Covers: maximum cap branch
 */
@Test
@DisplayName("Coverage Test: calculateFine - exceeds max")
public void testCalculateFine_ExceedsMax() {
    // 50 days would be very high, but capped at $25
    double fine = checkout.calculateFine(50, Book.BookType.FICTION);
    assertEquals(25.0, fine, 0.01, "Fine should be capped at $25.00");
}

//ISVALIDISBN() TESTS FOR COVERAGE 

/**
 * Test isValidISBN: null input
 * Covers: null check branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - null")
public void testIsValidISBN_Null() {
    assertFalse(checkout.isValidISBN(null), "Null should be invalid");
}

/**
 * Test isValidISBN: empty string
 * Covers: empty check branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - empty")
public void testIsValidISBN_Empty() {
    assertFalse(checkout.isValidISBN(""), "Empty should be invalid");
}

/**
 * Test isValidISBN: valid ISBN-10
 * Covers: valid 10-digit branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - valid ISBN-10")
public void testIsValidISBN_Valid10() {
    assertTrue(checkout.isValidISBN("0123456789"), "Valid ISBN-10 should pass");
}

/**
 * Test isValidISBN: valid ISBN-13
 * Covers: valid 13-digit branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - valid ISBN-13")
public void testIsValidISBN_Valid13() {
    assertTrue(checkout.isValidISBN("9780123456789"), "Valid ISBN-13 should pass");
}

/**
 * Test isValidISBN: valid with hyphens
 * Covers: hyphen removal branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - with hyphens")
public void testIsValidISBN_WithHyphens() {
    assertTrue(checkout.isValidISBN("978-0-123-45678-9"), "ISBN with hyphens should pass");
}

/**
 * Test isValidISBN: contains letters
 * Covers: non-digit character branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - contains letters")
public void testIsValidISBN_WithLetters() {
    assertFalse(checkout.isValidISBN("123456789X"), "ISBN with letters should fail");
}

/**
 * Test isValidISBN: wrong length
 * Covers: length validation branch
 */
@Test
@DisplayName("Coverage Test: isValidISBN - wrong length")
public void testIsValidISBN_WrongLength() {
    assertFalse(checkout.isValidISBN("12345"), "Wrong length should fail");
}
}
