# DFS Frontend - Distributed File Storage System

A modern React frontend for the Distributed File Storage System built with Raft consensus protocol.

## Features

- **File Upload**: Upload files with automatic chunking (1MB chunks)
- **File Download**: Download files by file ID and chunk IDs
- **Cluster Status**: Real-time monitoring of all nodes in the cluster
- **Leader Detection**: Automatically finds and uses the leader node for uploads
- **Multi-Node Support**: Can download from any available node

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend DFS nodes running on ports 8081, 8082, 8083 (or configure in `src/services/api.js`)

## Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

## Running the Application

Start the development server:
```bash
npm start
```

The application will open at `http://localhost:3000`

## Configuration

To change the backend node URLs, edit `src/services/api.js` and modify the `DEFAULT_NODES` array:

```javascript
const DEFAULT_NODES = [
  'http://localhost:8081',
  'http://localhost:8082',
  'http://localhost:8083'
];
```

## Building for Production

To create a production build:

```bash
npm run build
```

The build folder will contain the optimized production build.

## Usage

### Uploading Files

1. Click on the "Upload" tab
2. Select a file using the file picker
3. Enter a file ID (or use the auto-generated one based on filename)
4. Click "Upload File"
5. The file will be automatically chunked and uploaded to the leader node

### Downloading Files

1. Click on the "Download" tab
2. Enter the file ID
3. Optionally specify chunk IDs (comma-separated, e.g., "1,2,3")
4. Click "Download File"
5. The file will be downloaded from any available node

### Cluster Status

The cluster status panel shows:
- All nodes in the cluster
- Current role of each node (LEADER, FOLLOWER, etc.)
- Node availability (UP/DOWN)
- Auto-refreshes every 3 seconds

## Architecture

- **Components**: React functional components with hooks
- **API Service**: Centralized API client with leader detection
- **Chunking**: Files are split into 1MB chunks for upload
- **Error Handling**: Comprehensive error handling and user feedback

## Technologies

- React 18
- Axios for HTTP requests
- CSS3 for styling
- Modern ES6+ JavaScript


