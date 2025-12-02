import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './ClusterStatus.css';

const ClusterStatus = () => {
  const [nodeStatuses, setNodeStatuses] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchStatuses = async () => {
    setLoading(true);
    try {
      const statuses = await api.getAllNodeStatuses();
      setNodeStatuses(statuses);
    } catch (error) {
      console.error('Error fetching node statuses:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatuses();
    const interval = setInterval(fetchStatuses, 3000); // Refresh every 3 seconds
    return () => clearInterval(interval);
  }, []);

  const getRoleColor = (role) => {
    switch (role) {
      case 'LEADER':
        return '#4CAF50';
      case 'FOLLOWER':
        return '#2196F3';
      case 'CANDIDATE':
        return '#FF9800';
      default:
        return '#9E9E9E';
    }
  };

  const getStatusIcon = (status) => {
    return status === 'UP' ? '●' : '○';
  };

  return (
    <div className="cluster-status">
      <div className="cluster-header">
        <h2>Cluster Status</h2>
        <button onClick={fetchStatuses} disabled={loading} className="refresh-btn">
          {loading ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>
      <div className="nodes-grid">
        {nodeStatuses.map((node, index) => (
          <div key={index} className="node-card">
            <div className="node-header">
              <span className={`status-indicator ${node.status.toLowerCase()}`}>
                {getStatusIcon(node.status)}
              </span>
              <span className="node-url">{node.url}</span>
            </div>
            <div 
              className="node-role" 
              style={{ backgroundColor: getRoleColor(node.role) }}
            >
              {node.role}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ClusterStatus;


