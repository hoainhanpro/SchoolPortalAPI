// API base configuration
const API_BASE_URL = 'http://localhost:1234/api';

// Types for our API responses
export interface StudentScore {
  mssv: string;
  hoTen: string;
  maMon: string;
  tenMon: string;
  hocKy: string;
  namHoc: string;
  soTinChi: number;
  diemCC: number | null;    // Attendance score
  diemKT: number | null;    // Midterm score
  diemThi: number | null;   // Final exam score
  diemTB: number | null;    // Average score
}

// Tuition-related types
export interface HocKy {
  id: number;
  ten: string;
  namHoc: number;
  thuTu: number;
  startDate: string | null;
  endDate: string | null;
}

export interface HocPhi {
  id: number;
  mssv: string;
  hocKyId: number;
  tongTien: number;
  daDong: number;
  ngayDong: string | null;
  trangThai: string;
  // Embedded semester information
  tenHocKy: string;
  namHocHocKy: number;
  thuTuHocKy: number;
  startDateHocKy: string | null;
  endDateHocKy: string | null;
}

export interface DotDongHocPhi {
  id: number;
  hocKyId: number;
  ngayBatDau: string;
  ngayKetThuc: string;
  // Embedded semester information
  tenHocKy: string;
  namHocHocKy: number;
  thuTuHocKy: number;
  startDateHocKy: string | null;
  endDateHocKy: string | null;
}

export interface TuitionResponse {
  tuitionHistory: HocPhi[];
  paymentPeriods: DotDongHocPhi[];
  availableSemesters: HocKy[];
}

export interface PaymentRequest {
  semesterId: string;
  amount: number;
}

// API service for student-related operations
export class StudentApiService {
  private static getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('jwtToken');
    return {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    };
  }

  // Fetch student scores
  static async getStudentScores(): Promise<StudentScore[]> {
    try {
      const response = await fetch(`${API_BASE_URL}/student/scores`, {
        method: 'GET',
        headers: this.getAuthHeaders()
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Token expired or invalid, redirect to login
          localStorage.removeItem('jwtToken');
          window.location.href = '/';
          throw new Error('Authentication failed');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }      const scores: StudentScore[] = await response.json();
      return scores;
    } catch (error) {
      console.error('Error fetching student scores:', error);
      throw error;
    }
  }

  // Fetch tuition information
  static async getTuitionData(): Promise<TuitionResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/student/tuition`, {
        method: 'GET',
        headers: this.getAuthHeaders()
      });

      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('jwtToken');
          window.location.href = '/';
          throw new Error('Authentication failed');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const tuitionData: TuitionResponse = await response.json();
      return tuitionData;
    } catch (error) {
      console.error('Error fetching tuition data:', error);
      throw error;
    }
  }

  // Fetch tuition information for specific semester
  static async getTuitionDataBySemester(semesterId: string): Promise<TuitionResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}/student/tuition/semester?semesterId=${semesterId}`, {
        method: 'GET',
        headers: this.getAuthHeaders()
      });

      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('jwtToken');
          window.location.href = '/';
          throw new Error('Authentication failed');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const tuitionData: TuitionResponse = await response.json();
      return tuitionData;
    } catch (error) {
      console.error('Error fetching tuition data by semester:', error);
      throw error;
    }
  }

  // Process tuition payment
  static async processTuitionPayment(paymentData: PaymentRequest): Promise<HocPhi> {
    try {
      const response = await fetch(`${API_BASE_URL}/student/tuition/pay`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(paymentData)
      });

      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('jwtToken');
          window.location.href = '/';
          throw new Error('Authentication failed');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const updatedTuition: HocPhi = await response.json();
      return updatedTuition;
    } catch (error) {
      console.error('Error processing tuition payment:', error);
      throw error;
    }
  }
}

// Export the API service as default to make it a proper module
export default StudentApiService;
