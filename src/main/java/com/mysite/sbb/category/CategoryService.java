package com.mysite.sbb.category;

import com.mysite.sbb.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category create(String name) {
        Category category = new Category();
        category.setName(name);
        this.categoryRepository.save(category);
        return category;
    }

    public List<Category> getList() {
        return this.categoryRepository.findAll();
    }

    public Category getCategory(String name) {
        Optional<Category> category = this.categoryRepository.findByName(name);
        if (category.isPresent()) {
            return category.get();
        } else {
            throw new DataNotFoundException("Category not found");
        }
    }

    public void modify(Category category, String name) {
        category.setName(name);
        this.categoryRepository.save(category);
    }

}
