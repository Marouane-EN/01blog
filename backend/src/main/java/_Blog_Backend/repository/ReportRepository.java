package _Blog_Backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import _Blog_Backend.entity.Report;
import _Blog_Backend.types.ReportType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetIdAndReportType(Long reporterId, Long targetId, ReportType reportType);
}