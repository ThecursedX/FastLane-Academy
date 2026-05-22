package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.dto.StudentDTO;
import com.example.FastLane.Academy.service.StudentService;
import com.example.FastLane.Academy.util.SessionUtil;
import com.example.FastLane.Academy.util.VarList;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/getAll")
    public ResponseEntity<ResponseDTO> getAllStudents(HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }
        List<StudentDTO> students = studentService.getAllStudents();
        ResponseDTO response = new ResponseDTO();
        response.setCode(VarList.RSP_SUCCESS);
        response.setMessage("Students retrieved successfully");
        response.setContent(students);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getStudent/{studentId}")
    public ResponseEntity<ResponseDTO> getStudentById(@PathVariable String studentId) {
        ResponseDTO response = studentService.getStudentById(studentId);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.OK
                : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/update/{studentId}")
    public ResponseEntity<ResponseDTO> updateStudent(
            @PathVariable String studentId,
            @RequestBody StudentDTO dto, HttpSession session
    ) {
        if (!SessionUtil.isRole(session, "ADMIN") &&
                !SessionUtil.isRole(session, "STUDENT")) {

            return ResponseEntity.status(403)
                    .body(new ResponseDTO(
                            VarList.UNAUTHORIZED,
                            "Access denied",
                            null
                    ));
        }

        dto.setStudentId(studentId); // ensure ID consistency
        ResponseDTO response = studentService.updateStudent(dto);
        HttpStatus status = response.getCode().equals(VarList.UPDATED_SUCCESSFULLY)
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
    @PutMapping("/deactivate/{studentId}")
    public ResponseEntity<ResponseDTO> deactivateStudent(@PathVariable String studentId, HttpSession session)
    {
        if (!SessionUtil.isRole(session, "ADMIN")) {
            return ResponseEntity.status(403)
                    .body(new ResponseDTO(VarList.UNAUTHORIZED, "Admin access only", null));
        }

        ResponseDTO response = studentService.deactivateStudent(studentId);
        HttpStatus status = response.getCode().equals(VarList.RSP_SUCCESS)
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

}