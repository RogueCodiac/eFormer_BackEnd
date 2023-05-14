package eformer.back.eformer_backend.api;

import eformer.back.eformer_backend.model.Item;
import eformer.back.eformer_backend.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


@RestController
@RequestMapping("/api/items/")
public class ItemsApi {
    final ItemRepository manager;

    public ItemsApi(ItemRepository manager) {
        this.manager = manager;
    }

    public StringBuilder checkItem(Item item) {
        var error = new StringBuilder();

        if (item.getQuantity() <= 0) {
            error.append(String.format("Invalid quantity %d must be a positive integer\n",
                    item.getQuantity()));
        }

        if (manager.existsByNameIgnoreCase(item.getName())) {
            error.append(String.format("Invalid item name '%s' already taken\n",
                    item.getName()));
        }

        if (item.getUnitPrice() <= 0) {
            error.append(String.format("Invalid unit price %f must be a positive number\n",
                    item.getUnitPrice()));
        }

        if (item.getItemId() > 0) {
            error.append("Cannot supply own ID\n");
        }

        return error;
    }

    public StringBuilder checkItemForUpdate(Item item) {
        var error = new StringBuilder();

        if (item.getQuantity() <= 0) {
            error.append(String.format("Invalid quantity %d must be a positive integer\n",
                    item.getQuantity()));
        }

        if (!manager.existsByNameIgnoreCase(item.getName())) {
            error.append(String.format("Invalid item '%s' does not exist\n",
                    item.getName()));
        }

        if (item.getUnitPrice() <= 0) {
            error.append(String.format("Invalid unit price %f must be a positive number\n",
                    item.getUnitPrice()));
        }

        if (!manager.existsById(item.getItemId())) {
            error.append(String.format("Item (ID: %d) does not exist\n", item.getItemId()));
        }

        return error;
    }

    @GetMapping("getById")
    @ResponseBody
    public ResponseEntity<Object> getItemById(@RequestParam(name = "id") Integer itemId) {
        try {
            return manager.findByItemId(itemId)
                    .<ResponseEntity<Object>>map(item -> new ResponseEntity<>(item, HttpStatus.OK)) /* 200 */
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY)); /* 422 */
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @GetMapping("getByName")
    @ResponseBody
    public ResponseEntity<Object> getItemByName(@RequestParam(name = "name") String name) {
        try {
            return manager.findByName(name)
                    .<ResponseEntity<Object>>map(item -> new ResponseEntity<>(item, HttpStatus.OK)) /* 200 */
                    .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.UNPROCESSABLE_ENTITY)); /* 422 */
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @GetMapping("getAll")
    @ResponseBody
    public ResponseEntity<Object> getItems() {
        try {
            /* 200 */
            return new ResponseEntity<>(manager.findAll(), HttpStatus.OK);
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("create")
    @ResponseBody
    public ResponseEntity<Object> createItem(@RequestBody Item item) {
        try {
            var error = checkItem(item);

            if (error.length() > 0) {
                return new ResponseEntity<>(error.toString(), HttpStatus.UNPROCESSABLE_ENTITY); /* 422 */
            }

            return new ResponseEntity<>(manager.save(item), HttpStatus.OK); /* 200 */
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("getAllAfter")
    @ResponseBody
    public ResponseEntity<Object> getItemsAfter(@RequestBody Date date) {
        try {
            /* 200 */
            return new ResponseEntity<>(manager.findAllByIntroductionDateAfter(date), HttpStatus.OK);
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("getAllBefore")
    @ResponseBody
    public ResponseEntity<Object> getItemsBefore(@RequestBody Date date) {
        try {
            /* 200 */
            return new ResponseEntity<>(manager.findAllByIntroductionDateBefore(date), HttpStatus.OK);
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }

    @PostMapping("update")
    @ResponseBody
    public ResponseEntity<Object> updateItem(@RequestBody Item item) {
        try {
            var error = checkItemForUpdate(item);

            if (error.length() > 0) {
                return new ResponseEntity<>(error.toString(), HttpStatus.UNPROCESSABLE_ENTITY); /* 422 */
            }

            return new ResponseEntity<>(manager.save(item), HttpStatus.OK); /* 200 */
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }
}
