package com.gng.repo;

import com.gng.entity.FileRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * FileRequestLog Repository for CRUD operations.
 */
@Repository
public interface FileRequestLogRepository extends JpaRepository<FileRequestLog, Long> {}
