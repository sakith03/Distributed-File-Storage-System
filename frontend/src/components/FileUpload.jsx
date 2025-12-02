import React, { useState, useRef } from 'react';
import api from '../services/api';
import './FileUpload.css';

const CHUNK_SIZE = 1024 * 1024; // 1MB chunks

const FileUpload = ({ onUploadComplete }) => {
  const fileInputRef = useRef(null);
  const [file, setFile] = useState(null);
  const [fileId, setFileId] = useState('');
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      setFile(selectedFile);
      setFileId(selectedFile.name.replace(/[^a-zA-Z0-9]/g, '_'));
      setError(null);
      setSuccess(false);
    }
  };

  const handleFileIdChange = (e) => {
    setFileId(e.target.value);
  };

  const uploadFile = async () => {
    if (!file || !fileId) {
      setError('Please select a file and provide a file ID');
      return;
    }

    setUploading(true);
    setProgress(0);
    setError(null);
    setSuccess(false);

    try {
      const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
      const reader = new FileReader();

      for (let chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
        const start = chunkIndex * CHUNK_SIZE;
        const end = Math.min(start + CHUNK_SIZE, file.size);
        const chunk = file.slice(start, end);

        const chunkData = await new Promise((resolve, reject) => {
          reader.onload = (e) => resolve(e.target.result);
          reader.onerror = reject;
          reader.readAsArrayBuffer(chunk);
        });

        await api.uploadChunk(fileId, chunkIndex + 1, chunkData);
        setProgress(Math.round(((chunkIndex + 1) / totalChunks) * 100));
      }

      setSuccess(true);
      if (onUploadComplete) {
        onUploadComplete({ fileId, chunks: totalChunks });
      }
      
      // Reset form after 2 seconds
      setTimeout(() => {
        setFile(null);
        setFileId('');
        setProgress(0);
        setSuccess(false);
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
      }, 2000);
    } catch (error) {
      setError(error.response?.data || error.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="file-upload">
      <h2>Upload File</h2>
      <div className="upload-form">
        <div className="form-group">
          <label htmlFor="file-input">Select File</label>
          <input
            ref={fileInputRef}
            id="file-input"
            type="file"
            onChange={handleFileChange}
            disabled={uploading}
          />
          {file && (
            <div className="file-info">
              <span>Selected: {file.name}</span>
              <span>Size: {(file.size / 1024 / 1024).toFixed(2)} MB</span>
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="file-id">File ID</label>
          <input
            id="file-id"
            type="text"
            value={fileId}
            onChange={handleFileIdChange}
            placeholder="Enter file ID (e.g., myfile)"
            disabled={uploading}
          />
        </div>

        {uploading && (
          <div className="progress-container">
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{ width: `${progress}%` }}
              ></div>
            </div>
            <span className="progress-text">{progress}%</span>
          </div>
        )}

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        {success && (
          <div className="success-message">
            File uploaded successfully!
          </div>
        )}

        <button
          onClick={uploadFile}
          disabled={uploading || !file || !fileId}
          className="upload-btn"
        >
          {uploading ? 'Uploading...' : 'Upload File'}
        </button>
      </div>
    </div>
  );
};

export default FileUpload;


