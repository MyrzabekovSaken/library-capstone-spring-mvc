package com.library.app.controller;

import com.library.app.dto.BookDto;
import com.library.app.dto.UserDto;
import com.library.app.service.BookCopyService;
import com.library.app.service.BookService;
import com.library.app.service.OrderService;
import com.library.app.service.UserService;
import com.library.app.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the public-facing book catalog.
 * Allows users to browse and view details of books.
 */
@Controller
@RequestMapping("/")
public class BookController {
    private static final int PAGE_SIZE = 24;
    private static final String FIELD = "field";
    private static final String QUERY = "query";
    private static final String PAGE = "page";
    private static final String PAGINATION_DEFAULT_VALUE = "1";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String GENRE = "genre";
    private static final String BOOKS = "books";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String TOTAL_PAGES = "totalPages";
    private static final String ID = "id";
    private static final String BOOK = "book";
    private static final String AVAILABLE_COUNT = "availableCount";
    private static final String USER_STATUS = "userStatus";
    private static final String HAS_ACTIVE_ORDER = "hasActiveOrder";

    private final BookService bookService;
    private final BookCopyService bookCopyService;
    private final UserService userService;
    private final OrderService orderService;

    /**
     * Constructs an instance of {@code BookController} and initializes its dependencies
     *
     * @param bookService     the service for managing books
     * @param bookCopyService the service for managing individual book copies
     * @param userService     the service for managing user interactions
     * @param orderService    the service for processing book orders
     */
    @Autowired
    public BookController(BookService bookService, BookCopyService bookCopyService, UserService userService,
                          OrderService orderService) {
        this.bookService = bookService;
        this.bookCopyService = bookCopyService;
        this.userService = userService;
        this.orderService = orderService;
    }

    /**
     * Displays the paginated and filtered book catalog.
     *
     * @param field field to search by (title, author, genre)
     * @param query user input query
     * @param page  current page number
     * @param model model for view rendering
     * @return the catalog view
     */
    @GetMapping
    public String showCatalog(@RequestParam(name = FIELD, required = false) String field,
                              @RequestParam(name = QUERY, required = false) String query,
                              @RequestParam(name = PAGE, defaultValue = PAGINATION_DEFAULT_VALUE) int page,
                              Model model) {
        String title = null;
        String author = null;
        String genre = null;

        if (TITLE.equals(field)) {
            title = query;
        } else if (AUTHOR.equals(field)) {
            author = query;
        } else if (GENRE.equals(field)) {
            genre = query;
        }

        List<BookDto> filteredBooks = bookService.search(title, author, genre);
        int pageSize = PAGE_SIZE;
        List<BookDto> books = PaginationUtil.paginate(filteredBooks, page, pageSize);
        int totalPages = PaginationUtil.getTotalPages(filteredBooks.size(), pageSize);

        model.addAttribute(BOOKS, books);
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);
        model.addAttribute(FIELD, field);
        model.addAttribute(QUERY, query);

        return "book-catalog";
    }

    /**
     * Displays details of a single book, including available copies and user-related information.
     *
     * @param id        ID of the book
     * @param model     Spring model to populate data
     * @param principal current logged-in user
     * @return the book detail view or 404 page
     */
    @GetMapping("/book/{id}")
    public String getBookDetails(@PathVariable(ID) Long id, Model model, Principal principal) {
        Optional<BookDto> optionalBook = bookService.getById(id);

        if (optionalBook.isPresent()) {
            BookDto book = optionalBook.get();
            model.addAttribute(BOOK, book);
            int availableCount = bookCopyService.getAvailableCopiesCount(id);
            model.addAttribute(AVAILABLE_COUNT, availableCount);
            boolean hasActiveOrder = false;

            if (principal != null) {
                Optional<UserDto> userDto = userService.getDtoByUsername(principal.getName());

                if (userDto.isPresent()) {
                    hasActiveOrder = orderService.getActiveOrderForBook(book.getId(), userDto.get().getId());
                    model.addAttribute(USER_STATUS, userDto.get().getStatus());
                }
            }
            model.addAttribute(HAS_ACTIVE_ORDER, hasActiveOrder);

            return "book-detail";
        }
        return "redirect:/error/404";
    }
}
