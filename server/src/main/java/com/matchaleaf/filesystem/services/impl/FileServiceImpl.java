package com.matchaleaf.filesystem.services.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.matchaleaf.filesystem.dto.FileDownloadDto;
import com.matchaleaf.filesystem.dto.FileDto;
import com.matchaleaf.filesystem.dto.FileUploadDto;
import com.matchaleaf.filesystem.dto.IdResponseDto;
import com.matchaleaf.filesystem.entity.File;
import com.matchaleaf.filesystem.entity.Folder;
import com.matchaleaf.filesystem.repository.FileRepository;
import com.matchaleaf.filesystem.repository.FolderRepository;
import com.matchaleaf.filesystem.services.FileService;
import com.matchaleaf.filesystem.mapper.*;

//
@Service
public class FileServiceImpl implements FileService {

	@Autowired
	private FileRepository fileRepository;
	@Autowired
	private FolderRepository folderRepository;
//	@Autowired
//	private IdResponseDto idResponseDto;

	private FileMapper fileMapper;

//	@Autowired
//	Folder parentFolder;

	// private IdResponseDto testDto;
//	public FileServiceImpl() {
//
//	}

	public FileServiceImpl(FileRepository fileRepository, FileMapper fileMapper, FolderRepository folderRepository) {

		this.fileRepository = fileRepository;
		this.fileMapper = fileMapper;
		this.folderRepository = folderRepository;

	}

	@Override
	public FileDownloadDto downloadFileById(Integer id) {
		File fileEntity = new File();
		fileEntity = fileRepository.getOne(id);

		FileDownloadDto downloadDto = new FileDownloadDto();
		downloadDto.setName(fileEntity.getName());
		downloadDto.setData(fileEntity.getFileBytes());

		return downloadDto;
	}

	public File downloadFileBytesById(Integer ID) {

		return fileRepository.getOne(ID);

	}

	// Trashing a file, Update files parent folder id to the trash folder id
	public FileDto trashFile(Integer Id) {
		
		File fileToPatch = fileRepository.getOne(Id);
		Folder originFolder = folderRepository.getOne(fileToPatch.getParentFolder().getId());
		Folder trashFolder = folderRepository.getOne(2);
		
		Set<File> originFolderFiles = originFolder.getFiles();
		originFolderFiles.remove(fileToPatch);
		originFolder.setFiles(originFolderFiles);
		folderRepository.saveAndFlush(originFolder);
		
		
		Set<File> trashFolderFiles = trashFolder.getFiles();
		if(trashFolderFiles == null) { trashFolderFiles = new HashSet<>(); }
		trashFolderFiles.add(fileToPatch);
		trashFolder.setFiles(trashFolderFiles);
		folderRepository.saveAndFlush(trashFolder);
		
		fileToPatch.setParentFolder(trashFolder);
		fileRepository.saveAndFlush(fileToPatch);
		
		FileDto patchedFileDto = new FileDto();
		patchedFileDto.setId(fileToPatch.getId());
		patchedFileDto.setName(fileToPatch.getName());

		return patchedFileDto;
	}

	@Override
	public IdResponseDto createFile(MultipartFile file, Integer folderId) {

		Folder parentFolder = new Folder();
		parentFolder = folderRepository.getOne(folderId);

		File fileEntity = new File();

		fileEntity.setName(file.getOriginalFilename());
		try {
			fileEntity.setFileBytes(file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileEntity.setParentFolder(parentFolder);
		Set parentFolderSet = new HashSet<>();
		parentFolderSet = parentFolder.getFiles();
		parentFolderSet.add(fileEntity);

		parentFolder.setFiles(parentFolderSet);

		fileRepository.save(fileEntity);

		IdResponseDto idResponseDto = new IdResponseDto();
		idResponseDto.setId(fileEntity.getId());

		return (idResponseDto);

	}

	@Override
	public FileDto moveFile(Integer id, String name) {

		
		File fileToMove = fileRepository.getOne(id);
		Folder destinationFolder = folderRepository.getByName(name);
		Folder originFolder = folderRepository.getOne(fileToMove.getParentFolder().getId());
		
		Set<File> originFolderFiles = originFolder.getFiles();
		originFolderFiles.remove(fileToMove);
		originFolder.setFiles(originFolderFiles);
		folderRepository.saveAndFlush(originFolder);
		

		Set<File> destinationFolderFiles = destinationFolder.getFiles();
		if(destinationFolderFiles == null) { destinationFolderFiles = new HashSet(); }
		destinationFolderFiles.add(fileToMove);
		destinationFolder.setFiles(destinationFolderFiles);
		folderRepository.saveAndFlush(destinationFolder);
		
		fileToMove.setParentFolder(destinationFolder);
		fileRepository.saveAndFlush(fileToMove);
		

		return fileMapper.entityToFileDto(fileToMove);
	}

}
