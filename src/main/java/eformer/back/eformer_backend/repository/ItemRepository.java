package eformer.back.eformer_backend.repository;

import eformer.back.eformer_backend.model.Item;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    Optional<Item> findByItemId(Integer id);

    Optional<Item> findByName(String name);

    List<Item> findAllByIntroductionDateAfter(Date date);

    List<Item> findAllByIntroductionDateBefore(Date date);

    boolean existsByNameIgnoreCase(String name);
}
