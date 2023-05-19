package eformer.back.eformer_backend.api;

import eformer.back.eformer_backend.model.Item;
import eformer.back.eformer_backend.model.Order;
import eformer.back.eformer_backend.model.User;
import eformer.back.eformer_backend.repository.OrderRepository;
import eformer.back.eformer_backend.repository.UserRepository;
import eformer.back.eformer_backend.utility.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;


@RestController
@RequestMapping("/api/v1/orders/")
public class OrdersApi extends BaseApi {
    final OrderRepository manager;

    final UserRepository usersManager;

    final JwtService jService;

    public OrdersApi(OrderRepository manager,
                     UserRepository usersManager,
                     JwtService jService) {
        super(jService, usersManager);
        this.manager = manager;
        this.usersManager = usersManager;
        this.jService = jService;
    }

    public ResponseEntity<Object> getStatistics(HashMap<String, String> header,
                                                Integer type) {
        if (!isManager(header)) {
            /* 403 */
            return new ResponseEntity<>("User is not a manager", HttpStatus.FORBIDDEN);
        }

        Object result;

        switch (type) {
            case 1 -> result = manager.getTotalSales();
            case 2 -> result = manager.getAllPaid();
            case 3 -> result = manager.getTotalSoldQuantity();
            case 4 -> result = manager.getTotalActualSales();
            case 5 -> result = manager.findAll();
            default -> result = null;
        }

        if (result == null) {
            /* 400 */
            return new ResponseEntity<>("Internal error, contact IT", HttpStatus.BAD_REQUEST);
        }

        /* 200 */
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<Object> getOrders(HashMap<String, String> header,
                                           HashMap<String, Date> body,
                                           boolean isAfter) {
        try {
            var date = body.getOrDefault("date", null);

            if (date == null || header == null) {
                /* 422 */
                return new ResponseEntity<>("Missing sender or date fields",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            } else if (!canUserChange(header)) {
                /* 403 */
                return new ResponseEntity<>("Sender is not a manager",
                        HttpStatus.FORBIDDEN);
            }

            /* 200 */
            return new ResponseEntity<>(
                    isAfter ? manager.findAllByCreationDateAfter(date) :
                            manager.findAllByCreationDateBefore(date),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("getById")
    public ResponseEntity<Object> getById(
            @RequestHeader HashMap<String, String> header,
            @RequestBody Integer id
    ) {
        try {
            if (canUserChange(header)) {
                /* 200 */
                return new ResponseEntity<>(manager.findById(id).orElseThrow(), HttpStatus.OK);
            }

            /* 403 */
            return new ResponseEntity<>("Sender is not an employee",
                    HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("getAllByCustomer")
    public ResponseEntity<Object> getByCustomer(
            @RequestHeader HashMap<String, String> header,
            @RequestBody User customer
    ) {
        try {
            if (canUserChange(header)) {
                /* 200 */
                return new ResponseEntity<>(manager.findAllByCustomer(customer), HttpStatus.OK);
            }

            /* 403 */
            return new ResponseEntity<>("Sender is not an employee",
                    HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("getAllByEmployee")
    public ResponseEntity<Object> getByEmployee(
            @RequestHeader HashMap<String, String> header,
            @RequestBody User employee
    ) {
        try {
            if (canUserChange(header)) {
                /* 200 */
                return new ResponseEntity<>(manager.findAllByEmployee(employee), HttpStatus.OK);
            }

            /* 403 */
            return new ResponseEntity<>("Sender is not an employee",
                    HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Must include `customer` & `employee` in the body
     * */
    @PostMapping("getAllByCustomerAndEmployee")
    public ResponseEntity<Object> getByCustomerAndEmployee(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, User> users
    ) {
        try {
            var customer = users.get("customer");
            var employee = users.get("employee");

            if (canUserChange(header)) {
                /* 200 */
                return new ResponseEntity<>(manager.findAllByCustomerAndEmployee(customer, employee)
                        , HttpStatus.OK);
            }

            /* 403 */
            return new ResponseEntity<>("Sender is not an employee",
                    HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("getAllByStatus")
    public ResponseEntity<Object> getByStatus(
            @RequestHeader HashMap<String, String> header,
            @RequestBody String status
    ) {
        try {
            if (canUserChange(header)) {
                /* 200 */
                return new ResponseEntity<>(manager.findAllByStatus(status), HttpStatus.OK);
            }

            /* 403 */
            return new ResponseEntity<>("Sender is not an employee",
                    HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("getAllBeforeDate")
    public ResponseEntity<Object> getByDateBefore(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Date> body
    ) {
        return getOrders(header, body, false);
    }

    @PostMapping("getAllAfterDate")
    public ResponseEntity<Object> getByDateAfter(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Date> body
    ) {
        return getOrders(header, body, true);
    }

    @PostMapping("getAllBetweenDates")
    public ResponseEntity<Object> getByDateBetween(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Date> body
    ) {
        try {
            var start = body.getOrDefault("start", null);
            var end = body.getOrDefault("end", null);


            if (start == null || end == null || header == null) {
                /* 422 */
                return new ResponseEntity<>("Missing sender or date fields",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            } else if (!canUserChange(header)) {
                /* 403 */
                return new ResponseEntity<>("Sender is not a manager",
                        HttpStatus.FORBIDDEN);
            }

            /* 200 */
            return new ResponseEntity<>(
                    manager.findAllByCreationDateBetween(start, end),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("getTotalSales")
    public ResponseEntity<Object> getTotalSales(
            @RequestHeader HashMap<String, String> header
    ) {
        return getStatistics(header, 1);
    }

    @PostMapping("getAllPaid")
    public ResponseEntity<Object> getAllPaid(
            @RequestHeader HashMap<String, String> header
    ) {
        return getStatistics(header, 2);
    }

    @PostMapping("getTotalSoldQuantity")
    public ResponseEntity<Object> getTotalQuantity(
            @RequestHeader HashMap<String, String> header
    ) {
        return getStatistics(header, 3);
    }

    @PostMapping("getTotalActualSales")
    public ResponseEntity<Object> getTotalActualSales(
            @RequestHeader HashMap<String, String> header
    ) {
        return getStatistics(header, 4);
    }

    @PostMapping("getAll")
    public ResponseEntity<Object> getAll(
            @RequestHeader HashMap<String, String> header
    ) {
        return getStatistics(header, 5);
    }

    @PostMapping("confirm")
    public ResponseEntity<Object> confirm(
            @RequestHeader HashMap<String, String> header,
            @RequestBody Integer orderId
    ) {
        try {
            if (!canUserChange(header)) {
                /* 403 */
                return new ResponseEntity<>("User is not an employee", HttpStatus.FORBIDDEN);
            }

            var order = manager.findById(orderId).orElseThrow();
            order.confirm();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("cancel")
    public ResponseEntity<Object> cancel(
            @RequestHeader HashMap<String, String> header,
            @RequestBody Integer orderId
    ) {
        try {
            if (!canUserChange(header)) {
                /* 403 */
                return new ResponseEntity<>("User is not an employee", HttpStatus.FORBIDDEN);
            }

            var order = manager.findById(orderId).orElseThrow();
            order.cancel();

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("update")
    public ResponseEntity<Object> update(
            @RequestHeader HashMap<String, String> header,
            @RequestBody Order order
    ) {
        try {
            if (!canUserChange(header)) {
                /* 403 */
                return new ResponseEntity<>("User is not an employee", HttpStatus.FORBIDDEN);
            }

            manager.save(order);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("create")
    public ResponseEntity<Object> create(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Object> body
    ) {
        try {
            var customer = (User) body.get("user");
            var items = (HashMap<Item, Integer>) body.get("items");
            var employee = extractUser(header);

            if (!employee.isEmployee()) {
                /* 403 */
                return new ResponseEntity<>("User is not an employee", HttpStatus.FORBIDDEN);
            }

            var order = new Order(customer, employee, items);

            return new ResponseEntity<>(manager.save(order), HttpStatus.OK);
        } catch (Exception e) {
            /* 400 */
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
