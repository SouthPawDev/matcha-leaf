package com.matchaleaf.filesystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.matchaleaf.filesystem.model.FileModel.DBFile;

@Repository
public interface DBFileRepository extends JpaRepository<DBFile, String> {
	
	

}
