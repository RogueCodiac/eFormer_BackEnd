package eformer.back.eformer_backend.api;

import eformer.back.eformer_backend.model.User;
import eformer.back.eformer_backend.repository.OrderRepository;
import eformer.back.eformer_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;


@RestController
@RequestMapping("/api/orders/")
public class OrdersApi {
    final OrderRepository manager;
    final UserRepository usersManager;

    public OrdersApi(OrderRepository manager, UserRepository usersManager) {
        this.manager = manager;
        this.usersManager = usersManager;
    }

    public Optional<User> getActualUser(User user) {
        if (user == null) {
            return Optional.empty();
        }

        return usersManager.findByUsernameAndPassword(user.getUsername(), user.getPassword());
    }

    public boolean validAccess(HashMap<String, Object> params) {
        var user = (User) params.getOrDefault("user", null);
        var orderId = (Integer) params.getOrDefault("orderId", -1);

        if (user == null || orderId < 0) {
            return false;
        }

        var actualUser = getActualUser(user);
        var order = manager.findById(orderId);

        if (actualUser.isEmpty() || order.isEmpty()) {
            return false;
        }

        var customer = order.get().getCustomer();

        return customer.equals(actualUser.get()) || actualUser.get().isManager();
    }

    /**
     * Given JSON must contain the following fields:
     *  "orderId": ID of the order to fetch;
     *  "user": User fetching the order;
     * */
    @PostMapping("getById")
    @ResponseBody
    public ResponseEntity<Object> getOrderById(@RequestBody HashMap<String, Object> params) {
        try {
            if (validAccess(params)) {
                var order = manager.findById((Integer) params.get("orderId"));

                /* Order exists, check is for the compiler */
                if (order.isPresent()) {
                    return new ResponseEntity<>(order.get(), HttpStatus.OK);
                }
            }

            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    /**
     * Given JSON must contain the following fields:
     *  "customer": Customer to search for;
     *  "sender": User fetching the order;
     * */
    @PostMapping("getAllByCustomer")
    @ResponseBody
    public ResponseEntity<Object> getOrdersByCustomer(@RequestBody HashMap<String, Object> params) {
        var customer = (User) params.getOrDefault("customer", null);
        var sender = (User) params.getOrDefault("sender", null);

        var actualCustomer = getActualUser(customer);
        var actualSender = getActualUser(sender);

        if (actualCustomer.isEmpty() || actualSender.isEmpty()) {
            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!actualSender.get().equals(actualCustomer.get()) || !actualSender.get().isEmployee()) {
            /* 403 */
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        /* 200 */
        return new ResponseEntity<>(manager.findAllByCustomer(customer), HttpStatus.OK);
    }

    /**
     * Given JSON must contain the following fields:
     *  "sender": Employee fetching the order;
     * */
    @PostMapping("getAllByCustomer")
    @ResponseBody
    public ResponseEntity<Object> getOrdersByEmployee(@RequestBody HashMap<String, Object> params) {
        var sender = (User) params.getOrDefault("sender", null);

        var actualSender = getActualUser(sender);

        if (actualSender.isEmpty()) {
            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!actualSender.get().isEmployee()) {
            /* 403 */
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        /* 200 */
        return new ResponseEntity<>(manager.findAllByEmployee(sender), HttpStatus.OK);
    }


}
