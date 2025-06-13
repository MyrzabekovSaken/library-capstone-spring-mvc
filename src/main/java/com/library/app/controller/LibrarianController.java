package com.library.app.controller;

import com.library.app.dto.BookCopyDto;
import com.library.app.dto.BookDto;
import com.library.app.dto.OrderDto;
import com.library.app.dto.UserDto;
import com.library.app.mapper.BookCopyMapper;
import com.library.app.mapper.OrderMapper;
import com.library.app.mapper.UserMapper;
import com.library.app.service.BookCopyService;
import com.library.app.service.BookService;
import com.library.app.service.OrderService;
import com.library.app.service.UserService;
import com.library.app.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for librarian operations such as managing book orders and viewing copies.
 * Accessible only to users with role LIBRARIAN.
 */
@Controller
@RequestMapping("/librarian")
@PreAuthorize("hasRole('LIBRARIAN')")
public class LibrarianController {
    private static final int PAGE_SIZE = 10;
    private static final String PAGE = "page";
    private static final String PAGINATION_DEFAULT_VALUE = "1";
    private static final String ORDERS = "orders";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String TOTAL_PAGES = "totalPages";
    private static final String ORDER_ID = "orderId";
    private static final String DUE_DATE = "dueDate";
    private static final String FIELD = "field";
    private static final String QUERY = "query";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String GENRE = "genre";
    private static final String GROUPED_BOOKS = "groupedBooks";
    private static final String ID = "id";
    private static final String BOOK = "book";
    private static final String COPIES = "copies";
    private static final String ISSUED_USERS = "issuedUsers";
    private static final String READER_ORDERS = "readerOrders";
    private static final String PAGE_SIZE_ATTR = "pageSize";

    private final OrderService orderService;
    private final BookCopyService bookCopyService;
    private final BookService bookService;
    private final UserService userService;

    /**
     * Constructs an instance of {@code LibrarianController} and initializes its dependencies.
     *
     * @param orderService    the service for managing book orders
     * @param bookCopyService the service for handling individual book copies
     * @param bookService     the service for managing books
     * @param userService     the service for handling user interactions
     */
    @Autowired
    public LibrarianController(OrderService orderService, BookCopyService bookCopyService, BookService bookService,
                               UserService userService) {
        this.orderService = orderService;
        this.bookCopyService = bookCopyService;
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Displays a paginated list of all book orders.
     *
     * @param page  the current page number
     * @param model the model to provide attributes to the view
     * @return the order list page
     */
    @GetMapping("/orders")
    public String viewAllOrders(@RequestParam(name = PAGE, defaultValue = PAGINATION_DEFAULT_VALUE) int page,
                                Model model) {
        List<OrderDto> allOrders = orderService.getAllOrders().stream()
                .map(OrderMapper::toDto)
                .toList();
        int pageSize = PAGE_SIZE;
        List<OrderDto> pagedOrders = PaginationUtil.paginate(allOrders, page, pageSize);
        int totalPages = PaginationUtil.getTotalPages(allOrders.size(), pageSize);

        model.addAttribute(ORDERS, pagedOrders);
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);

        return "librarian/order-list";
    }

    /**
     * Confirms an order as issued by setting the due date and changing status.
     *
     * @param orderId the ID of the order to confirm
     * @param dueDate the due date for return
     * @return redirect to order list with success flag
     */
    @PostMapping("/orders/confirm")
    public String confirmOrder(@RequestParam(ORDER_ID) Long orderId,
                               @RequestParam(DUE_DATE)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        orderService.confirmOrderIssue(orderId, dueDate);

        return "redirect:/librarian/orders?success";
    }

    /**
     * Marks the specified order as returned.
     *
     * @param orderId the ID of the returned order
     * @return redirect to order list
     */
    @PostMapping("/orders/return")
    public String markReturned(@RequestParam(ORDER_ID) Long orderId) {
        orderService.markAsReturned(orderId);

        return "redirect:/librarian/orders";
    }

    /**
     * Displays a filtered and paginated list of books with their copies.
     *
     * @param field the field to filter by (title, author, genre)
     * @param query the filter query string
     * @param page  the page number
     * @param model the Spring model
     * @return the book list view
     */
    @GetMapping("/books")
    public String showAllBooks(@RequestParam(name = FIELD, required = false) String field,
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
        List<BookDto> paginatedBooks = PaginationUtil.paginate(filteredBooks, page, pageSize);
        int totalPages = PaginationUtil.getTotalPages(filteredBooks.size(), pageSize);

        Map<BookDto, List<BookCopyDto>> grouped = new LinkedHashMap<>();
        for (BookDto book : paginatedBooks) {
            List<BookCopyDto> copyDto = bookCopyService.getAllByBookId(book.getId()).stream()
                    .map(BookCopyMapper::toDto)
                    .toList();
            grouped.put(book, copyDto);
        }

        model.addAttribute(PAGE_SIZE_ATTR, pageSize);
        model.addAttribute(GROUPED_BOOKS, grouped.entrySet());
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);
        model.addAttribute(FIELD, field);
        model.addAttribute(QUERY, query);

        return "librarian/book-list";
    }

    /**
     * Displays details of a book and its copies, including issued users if applicable.
     *
     * @param id    the book ID
     * @param model the model with book and copy data
     * @return the book detail page or 404 redirect
     */
    @GetMapping("/books/{id}")
    public String viewBookCopies(@PathVariable(ID) Long id, Model model) {
        Optional<BookDto> book = bookService.getById(id);


        if (book.isPresent()) {
            Map<Long, String> issuedUsers = new HashMap<>();
            List<BookCopyDto> copies = bookCopyService.getAllByBookId(id).stream()
                    .map(BookCopyMapper::toDto)
                    .toList();

            for (BookCopyDto copy : copies) {
                orderService.getIssuedOrReserved(copy.getId())
                        .ifPresent(username -> issuedUsers.put(copy.getId(), username));
            }

            model.addAttribute(BOOK, book.get());
            model.addAttribute(COPIES, copies);
            model.addAttribute(ISSUED_USERS, issuedUsers);

            return "librarian/book-detail";
        }

        return "redirect:/error/404";
    }

    /**
     * Displays readers and their currently active orders.
     *
     * @param model the Spring model
     * @return reader list view
     */
    @GetMapping("/readers")
    public String showReadersOrders(Model model) {
        Map<UserDto, List<OrderDto>> readerOrders = userService.getReadersWithActiveOrders()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> UserMapper.toDto(entry.getKey()),
                        entry -> entry.getValue().stream().map(OrderMapper::toDto).toList()
                ));
        model.addAttribute(READER_ORDERS, readerOrders);

        return "librarian/reader-list";
    }
}
