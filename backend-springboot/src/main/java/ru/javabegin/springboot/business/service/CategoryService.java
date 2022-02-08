package ru.javabegin.springboot.business.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.javabegin.springboot.business.entity.Category;
import ru.javabegin.springboot.business.repository.CategoryRepository;

import javax.transaction.Transactional;
import java.util.List;

// всегда нужно создавать отдельный класс Service для доступа к данным, даже если кажется,
// что мало методов или это все можно реализовать сразу в контроллере
// Такой подход полезен для будущих доработок и правильной архитектуры (особенно, если работаете с транзакциями)

@Service
@Transactional
public class CategoryService {

    private CategoryRepository categoryRepository;

    @Autowired // добавляем возле конструктора - тогда во все внутренние параметры будут подставлены конкретные объекты
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // findAll метод без параметра - не имеет по задаче, поэтому его здесь нет
    public List<Category> findAll(String email){
        return categoryRepository.findByUserEmailOrderByTitleAsc(email);
    }

    public Category add(Category category) {
        return categoryRepository.save(category); // метод save обновляет или создает новый объект, если его не было
    }

    public Category update(Category category) {
        return categoryRepository.save(category); // метод save обновляет или создает новый объект, если его не было
    }

    // удаляем 1 объект по id
    public void delete(Long id) {
        categoryRepository.deleteById(id); // удаляем 1 объект по id
    }

    // поиск категорий пользователя по названию
    public List<Category> find(String title, String email) {
        return categoryRepository.findByTitle(title, email);
    }

    // находим 1 объект по id
    public Category findById(Long id) {
        return categoryRepository.findById(id).get(); // т.к. возвращается Optional - можно получить объект методом get()
    }

}
