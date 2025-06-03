package com.ptithcm.portal.service;

import com.ptithcm.portal.dto.TuitionResponseDto;
import com.ptithcm.portal.dto.HocPhiDto;
import com.ptithcm.portal.dto.DotDongHocPhiDto;

import java.util.List;

public interface TuitionService {

    /**
     * Lấy tất cả thông tin học phí của sinh viên
     * 
     * @param mssv Mã số sinh viên
     * @return Đối tượng chứa lịch sử học phí, đợt đóng học phí và danh sách học kỳ
     */
    TuitionResponseDto getTuitionInformation(String mssv);

    /**
     * Get comprehensive tuition data for a student
     * 
     * @param studentId The student ID
     * @return TuitionResponseDto containing all tuition data
     */
    TuitionResponseDto getTuitionData(String studentId);

    /**
     * Lấy thông tin học phí của sinh viên theo học kỳ
     * 
     * @param mssv    Mã số sinh viên
     * @param hocKyId ID học kỳ (có thể null để lấy tất cả)
     * @return Đối tượng chứa thông tin học phí đã lọc
     */
    TuitionResponseDto getTuitionInformationBySemester(String mssv, Integer hocKyId);

    /**
     * Get tuition data for a specific student and semester
     * 
     * @param studentId  The student ID
     * @param semesterId The semester ID
     * @return TuitionResponseDto containing filtered data for the specific semester
     */
    TuitionResponseDto getTuitionDataBySemester(String studentId, String semesterId);

    /**
     * Process a tuition payment for a student in a specific semester
     * 
     * @param studentId  The student ID
     * @param semesterId The semester ID
     * @param amount     The payment amount
     */
    void processPayment(String studentId, String semesterId, Double amount);

    /**
     * Lấy lịch sử học phí của sinh viên
     * 
     * @param mssv Mã số sinh viên
     * @return Danh sách lịch sử học phí
     */
    List<HocPhiDto> getTuitionHistory(String mssv);

    /**
     * Lấy thông tin các đợt đóng học phí
     * 
     * @param hocKyId ID học kỳ (có thể null để lấy tất cả)
     * @return Danh sách đợt đóng học phí
     */
    List<DotDongHocPhiDto> getPaymentPeriods(Integer hocKyId);

    /**
     * Cập nhật trạng thái thanh toán học phí
     * 
     * @param hocPhiId   ID bản ghi học phí
     * @param soTienDong Số tiền đóng
     * @return Bản ghi học phí đã cập nhật
     */
    HocPhiDto updatePaymentStatus(Integer hocPhiId, java.math.BigDecimal soTienDong);

    List<HocPhiDto> getAllHocPhi();

    HocPhiDto markAsPaid(String mssv, Integer hocKyId);
}
