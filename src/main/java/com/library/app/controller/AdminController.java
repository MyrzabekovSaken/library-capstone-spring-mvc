package com.library.app.controller;

import com.library.app.dto.*;
import com.library.app.mapper.BookCopyMapper;
import com.library.app.mapper.BookMapper;
import com.library.app.model.*;
import com.library.app.service.BookCopyService;
import com.library.app.service.BookService;
import com.library.app.service.OrderService;
import com.library.app.service.UserService;
import com.library.app.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controller for handling administrative functions such as managing users, books, copies, and viewing reports.
 * Accessible only to users with role ADMIN.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final int PAGE_SIZE = 10;
    private static final int BOOK_LIMIT = 5;
    private static final String PAGE = "page";
    private static final String PAGINATION_DEFAULT_VALUE = "1";
    private static final String USERS = "users";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String TOTAL_PAGES = "totalPages";
    private static final String ALL_USERS = "allUsers";
    private static final String ID = "id";
    private static final String USER = "user";
    private static final String ROLES = "roles";
    private static final String PASSWORD = "password";
    private static final String ACTIVE = "ACTIVE";
    private static final String BLOCKED = "BLOCKED";
    private static final String TITLE = "title";
    private static final String AUTHOR = "author";
    private static final String GENRE = "genre";
    private static final String BOOKS = "books";
    private static final String BOOK = "book";
    private static final String BOOK_COPIES = "bookCopies";
    private static final String MESSAGE = "message";
    private static final String COPY = "copy";
    private static final String ERROR = "error";
    private static final String COPY_ID = "copyId";
    private static final String TOTAL_BOOKS = "totalBooks";
    private static final String TOTAL_COPIES = "totalCopies";
    private static final String ISSUED_COPIES = "issuedCopies";
    private static final String COMPLETED_ORDERS = "completedOrders";
    private static final String ACTIVE_USERS = "activeUsers";
    private static final String TOP_BOOKS = "topBooks";
    private static final String TOP_USERS = "topUsers";
    private static final String PAGE_SIZE_ATTR = "pageSize";
    private static final String BOOK_NOT_FOUND_ID = "Book not found: id={}";
    private static final String SQL_STATE_23505 = "23505";
    private static final String DUPLICATE_INVENTORY_NUMBER = "Duplicate inventory number: {}";
    private static final String UNEXPECTED_ERROR = "Unexpected error";
    private static final String BOOK_UPDATE_SUCCESS = "book.update.success";
    private static final String BOOK_DELETE_SUCCESS = "book.delete.success";
    private static final String BOOK_NOT_FOUND_LOCALE = "book.not.found";
    private static final String COPY_UPDATE_SUCCESS = "copy.update.success";
    private static final String COPY_DELETE_SUCCESS = "copy.delete.success";
    private static final String COPY_DELETE_BOOK_REFERENCE_MISSING = "copy.delete.book.reference.missing";
    private static final String COPY_ADD_SUCCESS = "copy.add.success";
    private static final String COPY_ADD_DUPLICATE = "copy.add.duplicate";
    private static final String ERROR_UNEXPECTED = "error.unexpected";
    private static final String BOOK_ADD_SUCCESS = "book.add.success";
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private static final String BOOK_COPY_CANNOT_DELETE = "book.copy.cannotDelete";
    private static final String BOOK_COPY_CANNOT_EDIT = "book.copy.cannotEdit";

    private final UserService userService;
    private final BookService bookService;
    private final BookCopyService bookCopyService;
    private final OrderService orderService;
    private final MessageSource messageSource;

    /**
     * Constructs an instance of {@code AdminController} and initializes its dependencies.
     *
     * @param userService     the service for managing user-related operations
     * @param bookService     the service for managing books
     * @param bookCopyService the service for managing individual book copies
     * @param orderService    the service for managing book orders
     * @param messageSource   The source of the message, such as a user input or system-generated event.
     */
    @Autowired
    public AdminController(UserService userService, BookService bookService,
                           BookCopyService bookCopyService, OrderService orderService, MessageSource messageSource) {
        this.userService = userService;
        this.bookService = bookService;
        this.bookCopyService = bookCopyService;
        this.orderService = orderService;
        this.messageSource = messageSource;
    }

    /**
     * Retrieves a paginated list of admin users excluding the current logged-in user.
     *
     * @param page      current page number
     * @param model     the Spring model to add attributes
     * @param principal current logged-in user
     * @return the admin user list view
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(name = PAGE, defaultValue = PAGINATION_DEFAULT_VALUE) int page,
                            Model model, Principal principal) {
        String currentUsername = principal.getName();
        List<UserDto> allUsers = userService.getAllUserDtos().stream()
                .filter(user -> !user.getUsername().equals(currentUsername))
                .toList();
        int pageSize = PAGE_SIZE;
        List<UserDto> pagedUsers = PaginationUtil.paginate(allUsers, page, pageSize);
        int totalPages = PaginationUtil.getTotalPages(allUsers.size(), pageSize);

        model.addAttribute(USERS, pagedUsers);
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);
        model.addAttribute(ALL_USERS, allUsers);

        return "admin/user-list";
    }

    /**
     * Displays the form for editing a specific user.
     *
     * @param id    user ID
     * @param model the model to populate form data
     * @return the user edit view or redirect if not found
     */
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable(ID) Long id, Model model) {
        Optional<UserDto> optionalUser = userService.getDtoById(id);

        if (optionalUser.isPresent()) {
            model.addAttribute(USER, optionalUser.get());
            model.addAttribute(ROLES, Role.values());

            return "admin/user-edit";
        }

        return "redirect:/admin/users?error";
    }

    /**
     * Handles submission of updated user data.
     *
     * @param userDto  the user object with changes
     * @param password optional new password
     * @return redirect to the user list
     */
    @PostMapping("/users/edit")
    public String updateUser(@ModelAttribute(USER) UserDto userDto,
                             @RequestParam(name = PASSWORD, required = false) String password) {
        userService.updateUser(userDto, password);

        return "redirect:/admin/users?update";
    }

    /**
     * Toggles a user's status between ACTIVE and BLOCKED.
     *
     * @param id the ID of the user
     * @return redirect to user list
     */
    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable(ID) Long id) {
        Optional<UserDto> optionalUser = userService.getDtoById(id);

        if (optionalUser.isPresent()) {
            UserDto userDto = optionalUser.get();
            String newStatus = userDto.getStatus().equals(ACTIVE) ? BLOCKED : ACTIVE;
            userDto.setStatus(newStatus);
            userService.updateUser(userDto, null);
        }

        return "redirect:/admin/users";
    }

    /**
     * Deletes a user by their unique identifier and redirects to the admin user list.
     *
     * @param id the ID of the user to be deleted
     * @return a redirection string to the admin user management page
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable(ID) Long id) {
        userService.deleteUser(id);

        return "redirect:/admin/users";
    }

    /**
     * Displays the book search results with pagination and filters.
     *
     * @param title  the book title filter
     * @param author the book author filter
     * @param genre  the book genre filter
     * @param page   the requested page number
     * @param model  the model to store attributes for rendering the book list view
     * @return the view name for displaying the filtered and paginated book list
     */
    @GetMapping("/books")
    public String showBookList(@RequestParam(name = TITLE, required = false) String title,
                               @RequestParam(name = AUTHOR, required = false) String author,
                               @RequestParam(name = GENRE, required = false) String genre,
                               @RequestParam(name = PAGE, defaultValue = PAGINATION_DEFAULT_VALUE) int page,
                               Model model) {
        List<BookDto> bookList = bookService.search(title, author, genre);
        int pageSize = PAGE_SIZE;
        List<BookDto> pagedBooks = PaginationUtil.paginate(bookList, page, pageSize);
        int totalPages = PaginationUtil.getTotalPages(bookList.size(), pageSize);

        model.addAttribute(PAGE_SIZE_ATTR, pageSize);
        model.addAttribute(BOOKS, pagedBooks);
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);
        model.addAttribute(TITLE, title);
        model.addAttribute(AUTHOR, author);
        model.addAttribute(GENRE, genre);

        return "admin/book-list";
    }

    /**
     * Displays the form for adding a new book.
     *
     * @param model the model to store attributes for rendering the form
     * @return the view name for the book addition page
     */
    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute(BOOK, new BookDto());

        return "admin/book-add";
    }

    /**
     * Adds a new book to the system and redirects to the book list.
     *
     * @param bookDto            the book entity to be added
     * @param redirectAttributes Attributes used to pass success messages upon redirection
     * @param locale             the locale of the current user, used to fetch localized messages
     * @return the redirection string to the book list page
     */
    @PostMapping("/books/add")
    public String addBook(@ModelAttribute(BOOK) BookDto bookDto, RedirectAttributes redirectAttributes,
                          Locale locale) {
        bookService.saveBook(bookDto);
        String message = messageSource.getMessage(BOOK_ADD_SUCCESS, null, locale);
        redirectAttributes.addFlashAttribute(MESSAGE, message);

        return "redirect:/admin/books";
    }

    /**
     * Displays the details of a specific book along with its available copies.
     *
     * @param id    the unique identifier of the book
     * @param model the model to store attributes for rendering the book detail view
     * @return the view name for displaying book details or the "404" page if not found.
     */
    @GetMapping("/books/{id}")
    public String viewBookDetails(@PathVariable(ID) Long id, Model model) {
        Optional<BookDto> optionalBook = bookService.getById(id);

        if (optionalBook.isPresent()) {
            List<BookCopy> copies = bookCopyService.getAllByBookId(id);
            model.addAttribute(BOOK, optionalBook.get());
            model.addAttribute(BOOK_COPIES, copies);

            return "admin/book-detail";
        }

        return "error/page404";
    }

    /**
     * Displays the form for editing an existing book.
     *
     * @param id    the unique identifier of the book to be edited
     * @param model the model to store attributes for rendering the book edit form
     * @return the view name for editing the book or the "404" page if the book is not found.
     */
    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable(ID) Long id, Model model) {
        Optional<BookDto> bookOpt = bookService.getById(id);

        if (bookOpt.isPresent()) {
            model.addAttribute(BOOK, bookOpt.get());

            return "admin/book-edit";
        }

        return "error/page404";
    }

    /**
     * Updates an existing bookDto's details and redirects to the bookDto list.
     *
     * @param id                 the unique identifier of the bookDto to be updated
     * @param bookDto            the updated bookDto entity
     * @param redirectAttributes attributes used to pass success messages upon redirection
     * @return the redirection string to the bookDto list page
     */
    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable(ID) Long id,
                             @ModelAttribute(BOOK) BookDto bookDto,
                             RedirectAttributes redirectAttributes, Locale locale) {
        bookDto.setId(id);
        bookService.updateBook(bookDto);
        String message = messageSource.getMessage(BOOK_UPDATE_SUCCESS, null, locale);
        redirectAttributes.addFlashAttribute(MESSAGE, message);

        return "redirect:/admin/books";
    }

    /**
     * Deletes a book by its unique identifier and redirects to the book list.
     *
     * @param id                 the ID of the book to be deleted
     * @param redirectAttributes attributes used to pass success messages upon redirection
     * @return the redirection string to the book list page
     */
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable(ID) Long id, RedirectAttributes redirectAttributes, Locale locale) {
        bookService.deleteBook(id);
        String message = messageSource.getMessage(BOOK_DELETE_SUCCESS, null, locale);
        redirectAttributes.addFlashAttribute(MESSAGE, message);

        return "redirect:/admin/books";
    }

    /**
     * Displays the form for adding a new copy of a specific book.
     *
     * @param id    the unique identifier of the book for which a new copy is being added
     * @param model the model to store attributes for rendering the book copy form
     * @return the view name for adding a book copy or the "404" page if the book is not found
     */
    @GetMapping("/books/{id}/copies/new")
    public String showAddCopyForm(@PathVariable(ID) Long id, Model model) {
        Optional<BookDto> optionalBook = bookService.getById(id);

        if (optionalBook.isPresent()) {
            BookDto book = optionalBook.get();

            if (!model.containsAttribute(COPY)) {
                String nextInventoryNumber = bookCopyService.generateNextInventoryNumber(id);
                BookCopyDto copyDto = new BookCopyDto();
                copyDto.setInventoryNumber(nextInventoryNumber);
                copyDto.setBookId(book.getId());
                copyDto.setBookTitle(book.getTitle());
                model.addAttribute(COPY, copyDto);
            }
            model.addAttribute(BOOK, book);

            return "admin/book-copy-form";
        }

        return "error/page404";
    }

    /**
     * Saves a new copyDto of a specific book and redirects to the book details page.
     *
     * @param id                 the unique identifier of the book to which the copyDto belongs
     * @param copyDto            the book copyDto entity to be saved
     * @param redirectAttributes attributes used to pass messages upon redirection
     * @return the redirection string to the book details page or book list if not found
     */
    @PostMapping("/books/{id}/copies/new")
    public String saveNewCopy(@PathVariable(ID) Long id,
                              @ModelAttribute(COPY) BookCopyDto copyDto, Model model,
                              RedirectAttributes redirectAttributes, Locale locale) {
        Optional<BookDto> optionalBookDto = bookService.getById(id);

        if (optionalBookDto.isEmpty()) {
            logger.warn(BOOK_NOT_FOUND_ID, id);
            String error = messageSource.getMessage(BOOK_NOT_FOUND_LOCALE, null, locale);
            redirectAttributes.addFlashAttribute(ERROR, error);

            return "redirect:/admin/books";
        }

        BookDto bookDto = optionalBookDto.get();

        try {
            BookCopy copy = BookCopyMapper.toEntity(copyDto, BookMapper.toEntity(bookDto));
            bookCopyService.saveBook(copy);
            String successMessage = messageSource.getMessage(COPY_ADD_SUCCESS, null, locale);
            redirectAttributes.addFlashAttribute(MESSAGE, successMessage);

            return String.format("redirect:/admin/books/%s", id);

        } catch (RuntimeException e) {
            Throwable cause = e.getCause();

            model.addAttribute(COPY, copyDto);
            model.addAttribute(BOOK, bookDto);

            if (cause instanceof SQLException sqlEx && SQL_STATE_23505.equals(sqlEx.getSQLState())) {
                logger.warn(DUPLICATE_INVENTORY_NUMBER, copyDto.getInventoryNumber());
                String duplicateMessage = messageSource.getMessage(COPY_ADD_DUPLICATE, null, locale);
                model.addAttribute(ERROR, duplicateMessage);
            } else {
                logger.error(UNEXPECTED_ERROR, e);
                String unexpectedError = messageSource.getMessage(ERROR_UNEXPECTED, null, locale);
                model.addAttribute(ERROR, unexpectedError);
            }

            return "admin/book-copy-form";
        }
    }

    /**
     * Displays the form for editing an existing book copy.
     *
     * @param copyId the unique identifier of the book copy to be edited
     * @param model  the model to store attributes for rendering the book copy edit form
     * @return the view name for editing the book copy or the "404" page if not found
     */
    @GetMapping("/books/copies/edit/{copyId}")
    public String showEditCopyForm(@PathVariable(COPY_ID) Long copyId, Model model) {
        Optional<BookCopy> optionalBookCopy = bookCopyService.getById(copyId);

        if (optionalBookCopy.isPresent()) {
            BookCopy bookCopy = optionalBookCopy.get();
            BookCopyDto bookCopyDto = new BookCopyDto();
            bookCopyDto.setId(bookCopy.getId());
            bookCopyDto.setBookId(bookCopy.getBook().getId());
            bookCopyDto.setBookTitle(bookCopy.getBook().getTitle());
            bookCopyDto.setInventoryNumber(bookCopy.getInventoryNumber());
            bookCopyDto.setStatus(bookCopy.getStatus().name());
            model.addAttribute(COPY, bookCopyDto);
            model.addAttribute(BOOK, bookCopyDto);

            return "admin/book-copy-form";
        }

        return "error/page404";
    }

    /**
     * Updates an existing book bookCopyDto's details and redirects to the book details page.
     *
     * @param copyId             the unique identifier of the book bookCopyDto to be updated
     * @param bookCopyDto        the updated book bookCopyDto entity
     * @param redirectAttributes attributes used to pass success messages upon redirection
     * @return the redirection string to the book details page
     */
    @PostMapping("/books/copies/edit/{copyId}")
    public String updateCopy(@PathVariable(COPY_ID) Long copyId,
                             @ModelAttribute(COPY) BookCopyDto bookCopyDto, Model model,
                             RedirectAttributes redirectAttributes, Locale locale) {
        Optional<BookDto> optionalBook = bookService.getById(bookCopyDto.getBookId());

        if (optionalBook.isEmpty()) {
            String error = messageSource.getMessage(BOOK_NOT_FOUND_LOCALE, null, locale);
            redirectAttributes.addFlashAttribute(ERROR, error);

            return "redirect:/admin/books";
        }

        Optional<String> status = orderService.getIssuedOrReserved(copyId);
        if (status.isPresent()) {
            String error = messageSource.getMessage(BOOK_COPY_CANNOT_EDIT, new Object[]{status.get()}, locale);
            redirectAttributes.addFlashAttribute(ERROR, error);

            return String.format("redirect:/admin/books/%s", bookCopyDto.getBookId());
        }

        BookDto bookDto = optionalBook.get();
        BookCopy bookCopy = new BookCopy();
        bookCopy.setId(copyId);
        bookCopy.setInventoryNumber(bookCopyDto.getInventoryNumber());
        bookCopy.setStatus(CopyStatus.valueOf(bookCopyDto.getStatus()));

        Book book = new Book();
        book.setId(bookCopyDto.getBookId());
        bookCopy.setBook(book);

        try {
            bookCopyService.update(bookCopy);
            String message = messageSource.getMessage(COPY_UPDATE_SUCCESS, null, locale);
            redirectAttributes.addFlashAttribute(MESSAGE, message);

            return String.format("redirect:/admin/books/%s", bookCopyDto.getBookId());
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();

            model.addAttribute(COPY, bookCopyDto);
            model.addAttribute(BOOK, bookDto);

            if (cause instanceof SQLException sqlEx && SQL_STATE_23505.equals(sqlEx.getSQLState())) {
                logger.warn(DUPLICATE_INVENTORY_NUMBER, bookCopyDto.getInventoryNumber());
                String duplicateMessage = messageSource.getMessage(COPY_ADD_DUPLICATE, null, locale);
                model.addAttribute(ERROR, duplicateMessage);
            } else {
                logger.error(UNEXPECTED_ERROR, e);
                String unexpectedError = messageSource.getMessage(ERROR_UNEXPECTED, null, locale);
                model.addAttribute(ERROR, unexpectedError);
            }
        }

        return "admin/book-copy-form";
    }

    /**
     * Deletes a book copy by its unique identifier and redirects to the corresponding book details page.
     *
     * @param copyId             the unique identifier of the book copy to be deleted
     * @param redirectAttributes attributes used to pass success or error messages upon redirection
     * @return the redirection string to the book details page, book list page, or "404" page if not found
     */
    @PostMapping("/books/copies/delete/{copyId}")
    public String deleteCopy(@PathVariable(COPY_ID) Long copyId, RedirectAttributes redirectAttributes, Locale locale) {
        Optional<BookCopy> optionalBookCopy = bookCopyService.getById(copyId);

        if (optionalBookCopy.isPresent()) {
            BookCopy copy = optionalBookCopy.get();

            Optional<String> status = orderService.getIssuedOrReserved(copyId);
            if (status.isPresent()) {
                String error = messageSource.getMessage(BOOK_COPY_CANNOT_DELETE, new Object[]{status.get()}, locale);
                redirectAttributes.addFlashAttribute(ERROR, error);

                return String.format("redirect:/admin/books/%s", copy.getBook().getId());
            }

            if (copy.getBook() != null) {
                Long bookId = copy.getBook().getId();
                bookCopyService.deleteBook(copyId);
                String message = messageSource.getMessage(COPY_DELETE_SUCCESS, null, locale);
                redirectAttributes.addFlashAttribute(MESSAGE, message);

                return String.format("redirect:/admin/books/%s", bookId);
            }
            String error = messageSource.getMessage(COPY_DELETE_BOOK_REFERENCE_MISSING, null, locale);
            redirectAttributes.addFlashAttribute(ERROR, error);

            return "redirect:/admin/books";
        }

        return "error/page404";
    }

    /**
     * Displays the report dashboard with key statistics and analytics.
     *
     * @param model the model to store attributes for rendering the report dashboard
     * @return the view name for displaying the report dashboard
     */
    @GetMapping("/reports")
    public String showReportDashboard(Model model) {
        long totalBooks = bookService.countBooks();
        long totalCopies = bookCopyService.countAll();
        long issuedCopies = bookCopyService.countByStatus(CopyStatus.ISSUED);
        long completedOrders = orderService.getCountByStatuses(List.of(OrderStatus.RETURNED));
        long activeUsers = userService.countByStatus(ACTIVE);
        List<BookStatsDto> topBooks = orderService.getTopRequestedBooks(BOOK_LIMIT);
        List<UserStatsDto> topUsers = orderService.getTopActiveUsers(PAGE_SIZE);

        model.addAttribute(TOTAL_BOOKS, totalBooks);
        model.addAttribute(TOTAL_COPIES, totalCopies);
        model.addAttribute(ISSUED_COPIES, issuedCopies);
        model.addAttribute(COMPLETED_ORDERS, completedOrders);
        model.addAttribute(ACTIVE_USERS, activeUsers);
        model.addAttribute(TOP_BOOKS, topBooks);
        model.addAttribute(TOP_USERS, topUsers);

        return "admin/report-dashboard";
    }
}
