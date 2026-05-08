package com.example.FastLane.Academy.controller;

import com.example.FastLane.Academy.dto.InstructorDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.service.InstructorService;
import com.example.FastLane.Academy.util.VarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/instructors")
public class InstructorController {

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private ResponseDTO responseDTO;

    @PostMapping("/addInstructor")
    public ResponseEntity<ResponseDTO> addInstructor(
            @RequestBody InstructorDTO instructorDTO) {

        ResponseDTO response =
                instructorService.addInstructor(instructorDTO);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/getAllInstructors")
    public ResponseEntity getAllInstructors() {

        try {

            List<InstructorDTO> instructorDTOList =
                    instructorService.getAllInstructors();

            responseDTO.setCode(VarList.RSP_SUCCESS);
            responseDTO.setMessage("Success");
            responseDTO.setContent(instructorDTOList);

            return new ResponseEntity(
                    responseDTO,
                    HttpStatus.ACCEPTED
            );

        } catch (Exception ex) {

            responseDTO.setCode(VarList.RSP_ERROR);
            responseDTO.setMessage(ex.getMessage());
            responseDTO.setContent(null);

            return new ResponseEntity(
                    responseDTO,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/studentView")
    public ResponseEntity<ResponseDTO> getActiveInstructors() {

        ResponseDTO response =
                instructorService.getActiveInstructors();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @GetMapping("/getInstructor/{instructorId}")
    public ResponseEntity<ResponseDTO> getInstructorById(
            @PathVariable String instructorId) {

        ResponseDTO response =
                instructorService.getInstructorById(instructorId);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }

    @PutMapping("/updateInstructor/{instructorId}")
    public ResponseEntity<ResponseDTO> updateInstructor(
            @PathVariable String instructorId,
            @RequestBody InstructorDTO instructorDTO) {

        instructorDTO.setInstructorId(instructorId);

        ResponseDTO response =
                instructorService.updateInstructor(instructorDTO);

        HttpStatus status =
                response.getCode().equals(VarList.UPDATED_SUCCESSFULLY)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/deactivateInstructor/{instructorId}")
    public ResponseEntity<ResponseDTO> deactivateInstructor(
            @PathVariable String  instructorId) {

        ResponseDTO response =
                instructorService.deactivateInstructor(instructorId);

        HttpStatus status =
                response.getCode().equals(VarList.RSP_SUCCESS)
                        ? HttpStatus.ACCEPTED
                        : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/filterByVehicle")
    public ResponseEntity<ResponseDTO> getByVehicle(
            @RequestParam String vehicleType) {

        ResponseDTO response =
                instructorService.getInstructorsByVehicle(vehicleType);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(response);
    }
}
