package com.eventflow.service;

import com.eventflow.entity.EventCategory;
import com.eventflow.exception.ResourceNotFoundException;
import com.eventflow.repository.EventCategoryRepository;
import com.eventflow.util.SlugUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventCategoryService {

    private final EventCategoryRepository categoryRepository;

    public EventCategoryService(EventCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<EventCategory> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public List<EventCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public EventCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    public EventCategory findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Transactional
    public EventCategory saveCategory(String name, String description, String icon) {
        String slug = SlugUtils.toSlug(name);
        EventCategory category = new EventCategory(name, slug, description, icon);
        return categoryRepository.save(category);
    }
}
