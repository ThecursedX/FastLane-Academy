package com.example.FastLane.Academy.service;

import com.example.FastLane.Academy.dto.InstructorDTO;
import com.example.FastLane.Academy.dto.ResponseDTO;
import com.example.FastLane.Academy.entity.Instructor;
import com.example.FastLane.Academy.enums.InstructorStatus;
import com.example.FastLane.Academy.enums.LessonStatus;
import com.example.FastLane.Academy.repo.InstructorRepo;
import com.example.FastLane.Academy.repo.LessonRepo;
import com.example.FastLane.Academy.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InstructorService {

    @Autowired
    private InstructorRepo instructorRepo;

    @Autowired
    private LessonRepo lessonRepo;

    @Autowired
    private ModelMapper modelMapper;


    // Get All Instructors
    public List<InstructorDTO> getAllInstructors() {

        return instructorRepo.findAll().stream()
                .map(instructor ->
                        modelMapper.map(instructor, InstructorDTO.class))
                .toList();
    }

    // Student View (ACTIVE only + Bubble Sort)
    public ResponseDTO getActiveInstructors() {

        List<Instructor> instructors =
                instructorRepo.findByStatus(InstructorStatus.ACTIVE);

        // Bubble Sort by experience
        for (int i = 0; i < instructors.size() - 1; i++) {

            for (int j = 0; j < instructors.size() - i - 1; j++) {

                if (instructors.get(j).getExperienceYears() <
                        instructors.get(j + 1).getExperienceYears()) {

                    Instructor temp = instructors.get(j);
                    instructors.set(j, instructors.get(j + 1));
                    instructors.set(j + 1, temp);
                }
            }
        }

        List<InstructorDTO> instructorDTOList = instructors.stream()
                .map(instructor ->
                        modelMapper.map(instructor, InstructorDTO.class))
                .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Active instructors retrieved successfully", instructorDTOList);
    }

    // Get Instructor By ID
    public ResponseDTO getInstructorById(String instructorId) {

        Optional<Instructor> optionalInstructor =
                instructorRepo.findById(instructorId);

        if (optionalInstructor.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Instructor not found", null);
        }

        InstructorDTO instructorDTO =
                modelMapper.map(optionalInstructor.get(),
                        InstructorDTO.class);

        return new ResponseDTO(
                VarList.RSP_SUCCESS, "Instructor retrieved successfully", instructorDTO);
    }

    // Update Instructor
    public ResponseDTO updateInstructor(InstructorDTO instructorDTO) {

        Optional<Instructor> optionalInstructor =
                instructorRepo.findById(instructorDTO.getInstructorId());

        if (optionalInstructor.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND, "Instructor not found", instructorDTO);
        }

        Instructor instructor = optionalInstructor.get();

        // PATCH style updates

        if (instructorDTO.getInstructorName() != null) {
            instructor.setInstructorName(
                    instructorDTO.getInstructorName());
        }

        if (instructorDTO.getContactNumber() != null) {
            instructor.setContactNumber(
                    instructorDTO.getContactNumber());
        }

        if (instructorDTO.getExperienceYears() > 0) {
            instructor.setExperienceYears(
                    instructorDTO.getExperienceYears());
        }

        if (instructorDTO.getWorkingDays() != null) {
            instructor.setWorkingDays(
                    instructorDTO.getWorkingDays());
        }

        if (instructorDTO.getVehicleType() != null) {
            instructor.setVehicleType(
                    instructorDTO.getVehicleType());
        }

        instructorRepo.save(instructor);

        return new ResponseDTO(
                VarList.UPDATED_SUCCESSFULLY,
                "Instructor updated successfully",
                instructorDTO
        );
    }

    // Soft Delete / Deactivate
    public ResponseDTO deactivateInstructor(String instructorId) {

        Optional<Instructor> optionalInstructor =
                instructorRepo.findById(instructorId);

        if (optionalInstructor.isEmpty()) {

            return new ResponseDTO(
                    VarList.RSP_NO_DATA_FOUND,
                    "Instructor not found",
                    null
            );
        }

        Instructor instructor = optionalInstructor.get();

        boolean hasFutureLessons =
                lessonRepo.existsByInstructorIdAndDateGreaterThanEqualAndStatus(
                        instructorId.toString(),
                        LocalDate.now(),
                        LessonStatus.SCHEDULED
                );



        if (hasFutureLessons) {

            return new ResponseDTO(
                    VarList.INSTRUCTOR_HAS_FUTURE_LESSONS,
                    "Instructor has future scheduled lessons",
                    instructor
            );
        }

        instructor.setStatus(InstructorStatus.INACTIVE);

        instructorRepo.save(instructor);

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Instructor deactivated successfully",
                instructor
        );
    }

    // Filter By Vehicle Type
    public ResponseDTO getInstructorsByVehicle(String vehicleType) {

        List<InstructorDTO> instructorList =
                instructorRepo.findByStatus(InstructorStatus.ACTIVE)
                        .stream()
                        .filter(i -> i.getVehicleType()
                                .equalsIgnoreCase(vehicleType))
                        .map(i -> modelMapper.map(i, InstructorDTO.class))
                        .toList();

        return new ResponseDTO(
                VarList.RSP_SUCCESS,
                "Filtered instructors retrieved successfully",
                instructorList
        );
    }

}