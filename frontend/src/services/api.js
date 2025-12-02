import axios from 'axios';

// Default node URLs - can be configured
const DEFAULT_NODES = [
  'http://localhost:8081',
  'http://localhost:8082',
  'http://localhost:8083'
];

class ApiService {
  constructor() {
    this.nodes = [...DEFAULT_NODES];
    this.currentLeader = null;
  }

  // Find the leader node
  async findLeader() {
    if (this.currentLeader) {
      try {
        const response = await axios.get(`${this.currentLeader}/raft/role`, {
          responseType: 'text',
          timeout: 3000
        });
        const role = typeof response.data === 'string' ? response.data : response.data;
        if (role === 'LEADER') {
          return this.currentLeader;
        }
      } catch (e) {
        // Leader might have changed, continue to check all nodes
        this.currentLeader = null;
      }
    }

    // Check all nodes to find the leader
    for (const node of this.nodes) {
      try {
        const response = await axios.get(`${node}/raft/role`, {
          responseType: 'text',
          timeout: 3000
        });
        const role = typeof response.data === 'string' ? response.data : response.data;
        if (role === 'LEADER') {
          this.currentLeader = node;
          return node;
        }
      } catch (e) {
        // Node might be down, continue
        console.debug(`Node ${node} is not responding:`, e.message);
      }
    }
    return null;
  }

  // Get role of a specific node
  async getNodeRole(nodeUrl) {
    try {
      const response = await axios.get(`${nodeUrl}/raft/role`, {
        responseType: 'text', // Backend returns plain text
        timeout: 2000 // 2 second timeout
      });
      // Handle both string and object responses
      return typeof response.data === 'string' ? response.data : response.data;
    } catch (e) {
      // Silently handle connection errors - nodes may be down
      return 'UNKNOWN';
    }
  }

  // Upload a chunk
  async uploadChunk(fileId, chunkId, chunkData, nodeUrl = null) {
    const targetNode = nodeUrl || await this.findLeader();
    if (!targetNode) {
      throw new Error('No leader node available');
    }

    try {
      const response = await axios.post(
        `${targetNode}/files/${fileId}/chunks/${chunkId}`,
        chunkData,
        {
          headers: {
            'Content-Type': 'application/octet-stream'
          },
          maxBodyLength: Infinity,
          maxContentLength: Infinity
        }
      );
      return response.data;
    } catch (error) {
      if (error.response?.status === 307) {
        // Redirect to leader
        throw new Error('Not leader, please retry');
      }
      throw error;
    }
  }

  // Download a chunk
  async downloadChunk(fileId, chunkId, nodeUrl = null) {
    // Try to download from any available node
    const nodesToTry = nodeUrl ? [nodeUrl] : this.nodes;
    const errors = [];
    
    for (const node of nodesToTry) {
      try {
        const response = await axios.get(
          `${node}/files/${fileId}/chunks/${chunkId}`,
          {
            responseType: 'blob',
            timeout: 5000 // 5 second timeout for downloads
          }
        );
        // Check if we got a valid blob (not empty)
        if (response.data && response.data.size > 0) {
          return response.data;
        }
        // If blob is empty, try next node
        errors.push(`${node}: Empty response`);
      } catch (e) {
        // Collect error info but try next node
        const errorMsg = e.response?.status === 404 
          ? `${node}: Not found (404)`
          : e.code === 'ECONNREFUSED' || e.code === 'ERR_FAILED'
          ? `${node}: Connection refused`
          : `${node}: ${e.message}`;
        errors.push(errorMsg);
        continue;
      }
    }
    
    // All nodes failed
    const errorMessage = errors.length > 0 
      ? `Failed to download chunk ${chunkId} of ${fileId}. Errors: ${errors.join('; ')}`
      : `Chunk ${chunkId} of ${fileId} not found on any node`;
    throw new Error(errorMessage);
  }

  // Get all node statuses
  async getAllNodeStatuses() {
    const statuses = await Promise.all(
      this.nodes.map(async (node) => {
        try {
          const role = await this.getNodeRole(node);
          return { url: node, role: role || 'UNKNOWN', status: role !== 'UNKNOWN' ? 'UP' : 'DOWN' };
        } catch (e) {
          // Silently handle errors - nodes may be down
          return { url: node, role: 'UNKNOWN', status: 'DOWN' };
        }
      })
    );
    return statuses;
  }
}

export default new ApiService();


