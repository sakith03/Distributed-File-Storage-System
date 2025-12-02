import React, { useState } from 'react';
import './App.css';
import ClusterStatus from './components/ClusterStatus';
import FileUpload from './components/FileUpload';
import FileDownload from './components/FileDownload';

function App() {
  const [activeTab, setActiveTab] = useState('upload');

  return (
    <div className="App">
      <header className="app-header">
        <h1>Distributed File Storage System</h1>
        <p>Built with Raft Consensus Protocol</p>
      </header>

      <main className="app-main">
        <ClusterStatus />

        <div className="tabs">
          <button
            className={`tab ${activeTab === 'upload' ? 'active' : ''}`}
            onClick={() => setActiveTab('upload')}
          >
            Upload
          </button>
          <button
            className={`tab ${activeTab === 'download' ? 'active' : ''}`}
            onClick={() => setActiveTab('download')}
          >
            Download
          </button>
        </div>

        <div className="tab-content">
          {activeTab === 'upload' && <FileUpload />}
          {activeTab === 'download' && <FileDownload />}
        </div>
      </main>

      <footer className="app-footer">
        <p>DFS - Distributed File Storage System</p>
      </footer>
    </div>
  );
}

export default App;


