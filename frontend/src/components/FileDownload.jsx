import React, { useState } from 'react';
import api from '../services/api';
import './FileDownload.css';

const FileDownload = () => {
  const [fileId, setFileId] = useState('');
  const [chunkIds, setChunkIds] = useState('');
  const [downloading, setDownloading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const downloadFile = async () => {
    if (!fileId) {
      setError('Please provide a file ID');
      return;
    }

    setDownloading(true);
    setProgress(0);
    setError(null);
    setSuccess(false);

    try {
      // Parse chunk IDs or default to chunk 1
      let chunksToDownload = [1];
      if (chunkIds.trim()) {
        chunksToDownload = chunkIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        if (chunksToDownload.length === 0) {
          chunksToDownload = [1];
        }
      }

      const chunks = [];
      const totalChunks = chunksToDownload.length;

      for (let i = 0; i < chunksToDownload.length; i++) {
        const chunkId = chunksToDownload[i];
        const chunkData = await api.downloadChunk(fileId, chunkId);
        chunks.push({ id: chunkId, data: chunkData });
        setProgress(Math.round(((i + 1) / totalChunks) * 100));
      }

      // Combine chunks in order
      chunks.sort((a, b) => a.id - b.id);
      const blobParts = chunks.map(chunk => chunk.data);
      const blob = new Blob(blobParts, { type: 'application/octet-stream' });

      // Create download link
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileId;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);

      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        setFileId('');
        setChunkIds('');
        setProgress(0);
      }, 2000);
    } catch (error) {
      let errorMessage = 'Download failed';
      if (error.message) {
        errorMessage = error.message;
      } else if (error.response) {
        errorMessage = `Server error: ${error.response.status} - ${error.response.statusText}`;
      } else if (error.request) {
        errorMessage = 'No response from server. Please check if nodes are running.';
      }
      setError(errorMessage);
      console.error('Download error:', error);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="file-download">
      <h2>Download File</h2>
      <div className="download-form">
        <div className="form-group">
          <label htmlFor="download-file-id">File ID</label>
          <input
            id="download-file-id"
            type="text"
            value={fileId}
            onChange={(e) => setFileId(e.target.value)}
            placeholder="Enter file ID (e.g., myfile)"
            disabled={downloading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="chunk-ids">Chunk IDs (comma-separated, optional)</label>
          <input
            id="chunk-ids"
            type="text"
            value={chunkIds}
            onChange={(e) => setChunkIds(e.target.value)}
            placeholder="e.g., 1,2,3 (leave empty for chunk 1)"
            disabled={downloading}
          />
        </div>

        {downloading && (
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
            File downloaded successfully!
          </div>
        )}

        <button
          onClick={downloadFile}
          disabled={downloading || !fileId}
          className="download-btn"
        >
          {downloading ? 'Downloading...' : 'Download File'}
        </button>
      </div>
    </div>
  );
};

export default FileDownload;


