package com.vetclinic.controller;

import com.vetclinic.models.Reparto;
import com.vetclinic.service.RepartoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/reparti")
public class RepartoController {

    private final RepartoService departmentService;

    public RepartoController(RepartoService departmentService) {
        this.departmentService = departmentService;
    }


    @GetMapping("/list")
    public List<Reparto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }


    @PostMapping("/create")
    public ResponseEntity<Reparto> createDepartment(@RequestBody Reparto department) {
        Reparto createdDepartment = departmentService.createDepartment(department);
        return ResponseEntity.ok(createdDepartment);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Reparto> updateDepartment(@PathVariable Long id, @RequestBody Reparto department) {
        Reparto updatedDepartment = departmentService.updateDepartment(id, department);
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<Reparto> getDepartmentById(@PathVariable Long id) {
        Reparto department = departmentService.findDepartmentById(id);
        return ResponseEntity.ok(department);
    }
}
