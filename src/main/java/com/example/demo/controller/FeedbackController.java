package com.example.demo.controller;

import com.example.demo.entity.Feedback;
import com.example.demo.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository repo;

    @PostMapping
    public Feedback create(@RequestBody Feedback feedback) {
        return repo.save(feedback);
    }

    @GetMapping
    public List<Feedback> getAll() {
        return repo.findAll();
    }

    @PutMapping("/{id}")
    public Feedback update(@PathVariable Long id, @RequestBody Feedback newData) {
        Feedback f = repo.findById(id).orElseThrow();
        f.setRating(newData.getRating());
        f.setComment(newData.getComment());
        return repo.save(f);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}