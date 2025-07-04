package com.example.swd391_be_hiv.service;

import com.example.swd391_be_hiv.entity.Appointment;
import com.example.swd391_be_hiv.entity.Customer;
import com.example.swd391_be_hiv.entity.Doctor;
import com.example.swd391_be_hiv.model.request.AppointmentRequest;
import com.example.swd391_be_hiv.model.reponse.AppointmentResponse;
import com.example.swd391_be_hiv.model.reponse.AppointmentDetailResponse;
import com.example.swd391_be_hiv.repository.AppointmentRepository;
import com.example.swd391_be_hiv.repository.CustomerRepository;
import com.example.swd391_be_hiv.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public String bookAppointment(AppointmentRequest request) {
        Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
        Optional<Doctor> doctorOpt = doctorRepository.findById(request.getDoctorId());

        if (customerOpt.isEmpty() || doctorOpt.isEmpty()) {
            return "Invalid customer or doctor ID";
        }

        Appointment appointment = new Appointment();
        appointment.setCustomer(customerOpt.get());
        appointment.setDoctor(doctorOpt.get());
        appointment.setType(request.getType());
        appointment.setNote(request.getNote());
        appointment.setDatetime(request.getDatetime());
        appointment.setStatus("PENDING");

        appointmentRepository.save(appointment);
        return "Appointment booked successfully.";
    }

    public List<AppointmentResponse> getAppointmentsByCustomer(Long customerId) {
        List<Appointment> appointments = appointmentRepository.findByCustomer_Id(customerId);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointment() {
        List<Appointment> appointments = appointmentRepository.findByDeletedFalse();
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByDoctorId(Long doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor ID không được để trống");
        }
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndNotDeleted(doctorId);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public List<AppointmentResponse> getAppointmentsByDoctorIdAndStatus(Long doctorId, String status) {
        if (doctorId == null) {
            throw new IllegalArgumentException("Doctor ID không được để trống");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status không được để trống");
        }
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndStatusAndNotDeleted(doctorId, status);
        return appointments.stream()
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }


    public AppointmentDetailResponse getAppointmentDetail(Long appointmentId) {
        if (appointmentId == null) {
            throw new IllegalArgumentException("Appointment ID không được để trống");
        }

        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy appointment với ID: " + appointmentId);
        }

        Appointment appointment = appointmentOpt.get();
        if (appointment.isDeleted()) {
            throw new IllegalArgumentException("Appointment đã bị xóa");
        }

        return AppointmentDetailResponse.fromEntity(appointment);
    }


    public boolean hasDoctorAppointments(Long doctorId) {
        List<AppointmentResponse> appointments = getAppointmentsByDoctorId(doctorId);
        return !appointments.isEmpty();
    }

    public long countAppointmentsByDoctorId(Long doctorId) {
        List<AppointmentResponse> appointments = getAppointmentsByDoctorId(doctorId);
        return appointments.size();
    }


    public List<AppointmentResponse> getAppointmentsByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status không được để trống");
        }

        List<Appointment> appointments = appointmentRepository.findByDeletedFalse();
        return appointments.stream()
                .filter(appointment -> status.equalsIgnoreCase(appointment.getStatus()))
                .map(AppointmentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}