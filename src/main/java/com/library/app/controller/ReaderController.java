package com.library.app.controller;

import com.library.app.dto.BookDto;
import com.library.app.dto.OrderDto;
import com.library.app.mapper.OrderMapper;
import com.library.app.model.OrderType;
import com.library.app.service.BookService;
import com.library.app.service.OrderService;
import com.library.app.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for handling book orders made by users with the READER role.
 * Allows viewing, creating, and canceling orders.
 */
@Controller
@RequestMapping("/orders")
@PreAuthorize("hasRole('READER')")
public class ReaderController {
    private static final String PAGE = "page";
    private static final String SIZE = "size";
    private static final String PAGINATION_DEFAULT_VALUE = "1";
    private static final String PAGINATION_PAGE_SIZE = "10";
    private static final String ORDERS = "orders";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String TOTAL_PAGES = "totalPages";
    private static final String BOOK_ID = "bookId";
    private static final String BOOK = "book";
    private static final String ORDER_TYPES = "orderTypes";
    private static final String TYPE = "type";
    private static final String SUCCESS = "success";
    private static final String ORDER_CREATED_SUCCESSFULLY = "Order created successfully";
    private static final String ERROR = "error";
    private static final String ID = "id";
    private static final String FAILED_TO_CREATE_ORDER = "Failed to create order for user={} and bookId={}";
    private static final Logger logger = LoggerFactory.getLogger(ReaderController.class);

    private final OrderService orderService;
    private final BookService bookService;

    /**
     * Constructs an instance of {@code ReaderController} and initializes its dependencies.
     *
     * @param orderService orderService the service for managing book orders
     * @param bookService  bookService  the service for retrieving book details and availability
     */
    @Autowired
    public ReaderController(OrderService orderService, BookService bookService) {
        this.orderService = orderService;
        this.bookService = bookService;
    }

    /**
     * Displays a paginated list of the current user's orders.
     *
     * @param page      the page number to display
     * @param size      the number of orders per page
     * @param model     the model to pass attributes to the view
     * @param principal the currently logged-in user
     * @return the reader's order list view
     */
    @GetMapping
    public String viewOrders(@RequestParam(name = PAGE, defaultValue = PAGINATION_DEFAULT_VALUE) int page,
                             @RequestParam(name = SIZE, defaultValue = PAGINATION_PAGE_SIZE) int size,
                             Model model, Principal principal) {
        List<OrderDto> orderList = orderService.getOrdersByUsername(principal.getName()).stream()
                .map(OrderMapper::toDto)
                .toList();
        List<OrderDto> pagedOrders = PaginationUtil.paginate(orderList, page, size);
        int totalPages = PaginationUtil.getTotalPages(orderList.size(), size);

        model.addAttribute(ORDERS, pagedOrders);
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute(TOTAL_PAGES, totalPages);

        return "reader/order-list";
    }

    /**
     * Displays the form to request a book.
     *
     * @param bookId    the ID of the book to request
     * @param model     the model to populate with book and order types
     * @param principal the currently logged-in user
     * @return the order creation view or a 404 redirect if the book doesn't exist
     */
    @GetMapping("/request/{bookId}")
    public String showRequestForm(@PathVariable(BOOK_ID) Long bookId, Model model, Principal principal) {
        Optional<BookDto> bookDto = bookService.getById(bookId);

        if (bookDto.isPresent()) {
            model.addAttribute(BOOK, bookDto.get());
            model.addAttribute(ORDER_TYPES, OrderType.values());

            return "reader/order-create";
        }

        return "redirect:/error/404";
    }

    /**
     * Submits a new order request for the current user.
     *
     * @param bookId             the ID of the requested book
     * @param type               the type of the order (e.g. HOME, READING_ROOM)
     * @param principal          the currently logged-in user
     * @param redirectAttributes used to pass success or error messages
     * @return redirect to order list or back to the form on failure
     */
    @PostMapping("/request")
    public String submitOrder(@RequestParam(name = BOOK_ID, required = false) Long bookId,
                              @RequestParam(name = TYPE, required = false) OrderType type,
                              Principal principal, RedirectAttributes redirectAttributes) {

        try {
            orderService.createOrder(bookId, principal.getName(), type);
            redirectAttributes.addFlashAttribute(SUCCESS, ORDER_CREATED_SUCCESSFULLY);

            return "redirect:/orders";
        } catch (RuntimeException e) {
            logger.error(FAILED_TO_CREATE_ORDER, principal.getName(), bookId, e);
            redirectAttributes.addFlashAttribute(ERROR, e.getMessage());

            return String.format("redirect:/orders/request/%s", bookId);
        }
    }

    /**
     * Cancels an active order for the current user.
     *
     * @param orderId   the ID of the order to cancel
     * @param principal principal the currently logged-in user
     * @return redirect to order list with a cancellation flag
     */
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable(ID) Long orderId, Principal principal) {
        orderService.cancelOrder(orderId, principal.getName());

        return "redirect:/orders?canceled";
    }
}

