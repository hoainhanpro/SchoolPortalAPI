
-- ============================
-- TẠO CẤU TRÚC DATABASE
-- ============================

CREATE TABLE Khoa (
    id SERIAL PRIMARY KEY,
    ten TEXT NOT NULL
);

CREATE TABLE Lop (
    id SERIAL PRIMARY KEY,
    ten TEXT NOT NULL,
    khoa_id INT REFERENCES Khoa(id)
);

CREATE TABLE SinhVien (
    mssv TEXT PRIMARY KEY,
    ho TEXT NOT NULL,
    ten TEXT NOT NULL,
    ngay_sinh DATE NOT NULL,
    gioi_tinh TEXT NOT NULL,
    dia_chi TEXT NOT NULL,
    sdt TEXT NOT NULL,
    email TEXT NOT NULL,
    pwd_hash TEXT NOT NULL,
    lop_id INT REFERENCES Lop(id)
);

CREATE TABLE NhanVien (
    id SERIAL PRIMARY KEY,
    ho TEXT NOT NULL,
    ten TEXT NOT NULL,
    ngay_sinh DATE NOT NULL,
    gioi_tinh TEXT NOT NULL,
    dia_chi TEXT NOT NULL,
    sdt TEXT NOT NULL,
    email TEXT NOT NULL,
    pwd_hash TEXT NOT NULL,
    chuc_vu TEXT NOT NULL,
    khoa_id INT REFERENCES Khoa(id)
);

CREATE TABLE CoVan (
    lop_id INT REFERENCES Lop(id),
    nv_id INT REFERENCES NhanVien(id),
    nam_hoc INT NOT NULL,
    PRIMARY KEY (lop_id, nam_hoc, nv_id)
);

CREATE TABLE Mon (
    id SERIAL PRIMARY KEY,
    ten_mon TEXT NOT NULL,
    so_tin_chi INT NOT NULL,
    khoa_id INT REFERENCES Khoa(id)
);

CREATE TABLE HocKy (
    id SERIAL PRIMARY KEY,
    ten TEXT NOT NULL,
    nam_hoc INT NOT NULL,
    thu_tu INT NOT NULL
);

CREATE TABLE LopTinChi (
    id SERIAL PRIMARY KEY,
    mon_id INT REFERENCES Mon(id),
    hoc_ky_id INT REFERENCES HocKy(id),
    nhom INT NOT NULL,
    si_so_toi_da INT
);

CREATE TABLE ChiTietGiangDay (
    lop_tc_id INT REFERENCES LopTinChi(id),
    nv_id INT REFERENCES NhanVien(id),
    loai TEXT NOT NULL,
    PRIMARY KEY (lop_tc_id, nv_id)
);

CREATE TABLE Buoi (
    id SERIAL PRIMARY KEY,
    gio_bat_dau TIME NOT NULL,
    gio_ket_thuc TIME NOT NULL
);

CREATE TABLE PhongHoc (
    id SERIAL PRIMARY KEY,
    ten TEXT NOT NULL
);

CREATE TABLE lich (
    id SERIAL PRIMARY KEY,
    ngayhoc DATE NOT NULL,
    lop_tc_id INTEGER,
    buoi_id INTEGER NOT NULL,
    phong_hoc_id INTEGER,
    CONSTRAINT lich_phong_hoc_id_fkey FOREIGN KEY (phong_hoc_id) REFERENCES PhongHoc(id),
    CONSTRAINT lich_lop_tc_id_fkey FOREIGN KEY (lop_tc_id) REFERENCES loptinchi(id),
    CONSTRAINT lich_buoi_id_fkey FOREIGN KEY (buoi_id) REFERENCES buoi(id),
    CONSTRAINT unique_date_buoi UNIQUE (ngayhoc, buoi_id)
);

CREATE TABLE DotDangKy (
    id SERIAL PRIMARY KEY,
    hoc_ky_id INT REFERENCES HocKy(id),
    thoi_gian_bat_dau TIMESTAMP NOT NULL,
    thoi_gian_ket_thuc TIMESTAMP NOT NULL
);

CREATE TABLE DangKy (
    id SERIAL PRIMARY KEY,
    sv_id TEXT REFERENCES SinhVien(mssv),
    lop_tc_id INT REFERENCES LopTinChi(id),
    ngay_dang_ky TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trang_thai TEXT DEFAULT 'Chua xac nhan',
    diemcc NUMERIC DEFAULT 0,
    diemkt NUMERIC DEFAULT 0,
    diemthi NUMERIC DEFAULT 0,
    UNIQUE (sv_id, lop_tc_id)
);

CREATE TABLE DotDongHocPhi (
    id SERIAL PRIMARY KEY,
    hoc_ky_id INT REFERENCES HocKy(id),
    ngay_bat_dau DATE NOT NULL,
    ngay_ket_thuc DATE NOT NULL
);

CREATE TABLE HocPhi (
    id SERIAL PRIMARY KEY,
    sv_id TEXT REFERENCES SinhVien(mssv),
    hoc_ky_id INT REFERENCES HocKy(id),
    tong_tien NUMERIC NOT NULL,
    da_dong NUMERIC DEFAULT 0,
    ngay_dong DATE,
    trang_thai TEXT DEFAULT 'Chua dong'
);

CREATE TABLE ThongBao (
    id SERIAL PRIMARY KEY,
    nv_id INT REFERENCES NhanVien(id),
    tieu_de TEXT,
    noi_dung TEXT,
    ngay_gui TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE TinNhan (
    id SERIAL PRIMARY KEY,
    sv_id TEXT REFERENCES SinhVien(mssv),
    nv_id INT REFERENCES NhanVien(id),
    noi_dung TEXT,
    thoi_gian TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- THÊM DỮ LIỆU MẪU
-- ============================

INSERT INTO Khoa (ten) VALUES
('Công nghệ thông tin'),
('Kinh tế'),
('Điện - Điện tử');

INSERT INTO Lop (ten, khoa_id) VALUES
('CNTT1', 1),
('CNTT2', 1),
('KT1', 2),
('DĐT1', 3);

INSERT INTO NhanVien (ho, ten, ngay_sinh, gioi_tinh, dia_chi, sdt, email, chuc_vu, khoa_id, pwd_hash) VALUES
('Nguyen', 'Van A', '1980-05-01', 'Nam', 'Hà Nội', '0123456789', 'nva@example.com', 'Giao vien', 1, '$2y$10$cVDnFPmxjebZleWLqGSQje2XAftUWqskXSQAb1zprTDr2OW7eE.uK'),
('Tran', 'Thi B', '1985-07-12', 'Nu', 'TP.HCM', '0987654321', 'ttb@example.com', 'Giao vien', 2, '$2y$10$cVDnFPmxjebZleWLqGSQje2XAftUWqskXSQAb1zprTDr2OW7eE.uK'),
('Le', 'Van C', '1990-09-23', 'Nam', 'Đà Nẵng', '0111222333', 'lvc@example.com', 'Nhan vien', 1,'$2y$10$cVDnFPmxjebZleWLqGSQje2XAftUWqskXSQAb1zprTDr2OW7eE.uK');

INSERT INTO CoVan (lop_id, nv_id, nam_hoc) VALUES
(1, 1, 2024),
(3, 2, 2024);

INSERT INTO SinhVien (mssv, ho, ten, ngay_sinh, gioi_tinh, dia_chi, sdt, email, lop_id, pwd_hash) VALUES
('SV001', 'Pham', 'Minh Tuan', '2003-03-15', 'Nam', 'Hà Nội', '0900111222', 'pmt@example.com', 1, '$2y$10$cVDnFPmxjebZleWLqGSQje2XAftUWqskXSQAb1zprTDr2OW7eE.uK'),
('SV002', 'Nguyen', 'Thi Lan', '2003-08-20', 'Nu', 'TP.HCM', '0933444555', 'ntl@example.com', 3, '$2y$10$cVDnFPmxjebZleWLqGSQje2XAftUWqskXSQAb1zprTDr2OW7eE.uK');

INSERT INTO Mon (ten_mon, so_tin_chi, khoa_id) VALUES
('Cơ sở dữ liệu', 3, 1),
('Kinh tế vi mô', 3, 2);

INSERT INTO HocKy (ten, nam_hoc, thu_tu) VALUES
('Học kỳ 1', 2024, 1),
('Học kỳ 2', 2024, 2);

INSERT INTO LopTinChi (mon_id, hoc_ky_id, nhom, si_so_toi_da) VALUES
(1, 1, 1, 60),
(2, 1, 1, 50);

INSERT INTO ChiTietGiangDay (lop_tc_id, nv_id, loai) VALUES
(1, 1, 'Ly thuyet'),
(2, 2, 'Ly thuyet');

INSERT INTO Buoi (gio_bat_dau, gio_ket_thuc) VALUES
('07:00', '11:00'),
('13:00', '17:00');

INSERT INTO PhongHoc (ten) VALUES
('P101'),
('P102');

INSERT INTO Lich (ngayhoc,buoi_id, lop_tc_id) VALUES
('12/05/2025', 1, 1),
('13/05/2025', 2, 2);

INSERT INTO DotDangKy (hoc_ky_id, thoi_gian_bat_dau, thoi_gian_ket_thuc) VALUES
(1, '2024-07-01 08:00:00', '2024-07-15 23:59:59');

INSERT INTO DangKy (sv_id, lop_tc_id, ngay_dang_ky, trang_thai, diemcc, diemkt, diemthi) VALUES
('SV001', 1, CURRENT_TIMESTAMP, 'Da xac nhan', 9, 8, 7),
('SV002', 2, CURRENT_TIMESTAMP, 'Chua xac nhan', 0, 0, 0),
('SV001', 2, CURRENT_TIMESTAMP, 'Chua xac nhan',0,0,0);

INSERT INTO DotDongHocPhi (hoc_ky_id, ngay_bat_dau, ngay_ket_thuc) VALUES
(1, '2024-07-20', '2024-08-10');

INSERT INTO HocPhi (sv_id, hoc_ky_id, tong_tien, da_dong, ngay_dong, trang_thai) VALUES
('SV001', 1, 1500000, 1500000, '2024-07-25', 'Da dong'),
('SV002', 1, 1500000, 0, NULL, 'Chua dong');

INSERT INTO ThongBao (nv_id, tieu_de, noi_dung) VALUES
(3, 'Thông báo học phí', 'Hạn đóng học phí là 10/08/2024.');

INSERT INTO TinNhan (sv_id, nv_id, noi_dung) VALUES
('SV001', 3, 'Em đã nộp học phí rồi ạ.');
