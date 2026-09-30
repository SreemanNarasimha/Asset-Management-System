import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE = import.meta.env.VITE_API_BASE || 'https://asset-management-system-production-acf9.up.railway.app/api';

function Login({ setAuth }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/auth/login`, { username, password });
      localStorage.setItem('token', res.data.token);
      setAuth(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <div className="login-container">
      <h2 className="login-title">Military AMS</h2>
      {error && <p className="login-error">{error}</p>}
      <form onSubmit={handleLogin} className="login-form">
        <div>
          <label>Username</label>
          <input className="form-input" value={username} onChange={e => setUsername(e.target.value)} required />
        </div>
        <div>
          <label>Password</label>
          <input className="form-input" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
       <div>
        <button className="login-button" type="submit">Login</button> 
      </div>
      </form>
    </div>
  );
}

function Layout({ auth, logout, children }) {
  return (
    <div className="layout-container">
      <aside className="sidebar">
        <h2>Control Panel</h2>
        <p className="sidebar-role">{auth.username} - {auth.role}</p>
        <nav className="sidebar-nav">
          <Link to="/" className="nav-link">Dashboard</Link>
          <Link to="/inventory" className="nav-link">Inventory</Link>
          <Link to="/audit-logs" className="nav-link">Audit Logs</Link>
          {auth?.role === 'ADMIN' && (
            <>
              <Link to="/bases" className="nav-link">Bases</Link>
              <Link to="/users" className="nav-link">System Users</Link>
              <Link to="/equipment-types" className="nav-link">Equipment Types</Link>
            </>
          )}
          {(auth?.role === 'ADMIN' || auth?.role === 'BASE_COMMANDER') && (
            <>
              <Link to="/personnel" className="nav-link">Personnel</Link>
              <Link to="/assignments" className="nav-link">Assignments</Link>
              <Link to="/expenditures" className="nav-link">Expenditures</Link>
            </>
          )}
          {(auth?.role === 'ADMIN' || auth?.role === 'BASE_COMMANDER' || auth?.role === 'LOGISTICS_OFFICER') && (
            <>
              <Link to="/transfers" className="nav-link">Transfers</Link>
              <Link to="/purchases" className="nav-link">Purchases</Link>
            </>
          )}
          <button onClick={logout} className="logout-button">Logout</button>
        </nav>
      </aside>
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}

function Dashboard({ auth }) {
  const [data, setData] = useState({ available: 0, assigned: 0, transfers: 0, openingBalance: 0, closingBalance: 0, netMovement: 0, purchases: 0, transfersIn: 0, transfersOut: 0, expended: 0 });
  const [filters, setFilters] = useState({ date: '', baseId: '', equipmentTypeId: '' });
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);
  const [showNetMovementModal, setShowNetMovementModal] = useState(false);
  const [auditLogs, setAuditLogs] = useState([]);

  useEffect(() => {
    axios.get(`${API_BASE}/bases`).then(res => setBases(res.data.content || res.data)).catch(console.error);
    axios.get(`${API_BASE}/equipment-types`).then(res => setEquipmentTypes(res.data.content || res.data)).catch(console.error);
  }, []);

  useEffect(() => {
    const params = new URLSearchParams();
    if (filters.date) params.append('date', filters.date);
    if (filters.baseId) params.append('baseId', filters.baseId);
    if (filters.equipmentTypeId) params.append('equipmentTypeId', filters.equipmentTypeId);
    
    axios.get(`${API_BASE}/dashboard/summary?${params.toString()}`).then(res => setData(res.data)).catch(console.error);

    if (auth?.role === 'ADMIN') {
      axios.get(`${API_BASE}/audit-logs`).then(res => setAuditLogs(res.data)).catch(console.error);
    }
  }, [filters, auth?.role]);

  useEffect(() => {
    let interval;
    if (auth?.role === 'ADMIN') {
      interval = setInterval(() => {
        axios.get(`${API_BASE}/audit-logs`).then(res => setAuditLogs(res.data)).catch(() => {});
      }, 5000);
    }
    return () => clearInterval(interval);
  }, [auth?.role]);

  return (
    <div>
      <h2 className="dashboard-title">
        {auth?.role === 'ADMIN' ? 'Global Command Dashboard' : `Base Dashboard (Commanding Base ID: ${auth?.baseId})`}
      </h2>

      <div style={{ marginBottom: '2rem', display: 'flex', gap: '1rem', background: 'white', padding: '1rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Date Filter</label>
          <input type="date" className="form-input" value={filters.date} onChange={e => setFilters({...filters, date: e.target.value})} />
        </div>
        {auth?.role === 'ADMIN' && (
          <div>
            <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Base</label>
            <select className="form-input" value={filters.baseId} onChange={e => setFilters({...filters, baseId: e.target.value})}>
              <option value="">All Bases</option>
              {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
        )}
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Equipment Type</label>
          <select className="form-input" value={filters.equipmentTypeId} onChange={e => setFilters({...filters, equipmentTypeId: e.target.value})}>
            <option value="">All Equipment</option>
            {equipmentTypes.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
          </select>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3 className="card-title">Opening Balance</h3>
          <p className="card-value">{data.openingBalance || 0}</p>
        </div>
        <div className="dashboard-card" style={{ cursor: 'pointer', border: '2px solid #27ae60' }} onClick={() => setShowNetMovementModal(true)}>
          <h3 className="card-title" style={{ color: '#27ae60' }}>Net Movement 🛈</h3>
          <p className="card-value">{data.netMovement || 0}</p>
        </div>
        <div className="dashboard-card">
          <h3 className="card-title">Closing Balance</h3>
          <p className="card-value">{data.closingBalance || 0}</p>
        </div>
        <div className="dashboard-card" style={{ border: '2px solid #2980b9' }}>
          <h3 className="card-title" style={{ color: '#2980b9' }}>Available Assets</h3>
          <p className="card-value">{data.available || 0}</p>
        </div>
        <div className="dashboard-card">
          <h3 className="card-title">Assigned Assets</h3>
          <p className="card-value">{data.assigned || 0}</p>
        </div>
        <div className="dashboard-card">
          <h3 className="card-title">Expended Assets</h3>
          <p className="card-value">{data.expended || 0}</p>
        </div>
      </div>

      {showNetMovementModal && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ background: 'white', padding: '2rem', borderRadius: '8px', minWidth: '300px' }}>
            <h3 style={{ margin: '0 0 1.5rem 0' }}>Net Movement Details</h3>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span>Purchases:</span>
              <strong>{data.purchases || 0}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span>Transfers In (+):</span>
              <strong>{data.transfersIn || 0}</strong>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', color: '#e74c3c' }}>
              <span>Transfers Out (-):</span>
              <strong>{data.transfersOut || 0}</strong>
            </div>
            <div style={{ borderTop: '1px solid #eee', paddingTop: '1rem', display: 'flex', justifyContent: 'space-between', fontWeight: 'bold' }}>
              <span>Total Net Movement:</span>
              <span>{data.netMovement || 0}</span>
            </div>
            <button onClick={() => setShowNetMovementModal(false)} style={{ marginTop: '2rem', width: '100%', padding: '0.5rem', background: '#333', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Close</button>
          </div>
        </div>
      )}

    </div>
  );
}

function GenericForm({ title, endpoint, fields, allowFilters }) {
  const [form, setForm] = useState({});
  const [error, setError] = useState('');
  const [items, setItems] = useState([]);
  
  const [filters, setFilters] = useState({ date: '', equipmentTypeId: '' });
  const [equipmentTypes, setEquipmentTypes] = useState([]);

  useEffect(() => {
    if (allowFilters) {
      axios.get(`${API_BASE}/equipment-types`).then(res => setEquipmentTypes(res.data.content || res.data)).catch(console.error);
    }
  }, [allowFilters]);

  const fetchItems = async () => {
    try {
      let url = `${API_BASE}/${endpoint}`;
      if (allowFilters) {
        const params = new URLSearchParams();
        if (filters.date) params.append('date', filters.date);
        if (filters.equipmentTypeId) params.append('equipmentTypeId', filters.equipmentTypeId);
        if (params.toString()) url += `?${params.toString()}`;
      }
      const res = await axios.get(url);
      if (res.data.content) setItems(res.data.content);
      else if (Array.isArray(res.data)) setItems(res.data);
      else setItems([]);
    } catch (err) {
      console.error('Failed to fetch items', err);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [endpoint, filters]);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      const payload = {};
      for (const key in form) {
        if (form[key] === '') continue;
        if (key.includes('.')) {
          const [parent, child] = key.split('.');
          if (!payload[parent]) payload[parent] = {};
          payload[parent][child] = form[key];
        } else {
          payload[key] = form[key];
        }
      }

      await axios.post(`${API_BASE}/${endpoint}`, payload);
      alert(`${title} created successfully!`);
      setError('');
      setForm({});
      e.target.reset();
      fetchItems();
    } catch (err) {
      setError(err.response?.data?.message || `Error creating ${title}`);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm(`Are you sure you want to delete this record?`)) {
      try {
        await axios.delete(`${API_BASE}/${endpoint}/${id}`);
        fetchItems();
      } catch (err) {
        alert(err.response?.data?.message || 'Error deleting record');
      }
    }
  };

  const getNestedValue = (obj, path) => {
    const value = path.split('.').reduce((acc, part) => acc && acc[part], obj);
    if (typeof value === 'object' && value !== null) {
      return value.name || value.id || JSON.stringify(value);
    }
    return value;
  };

  return (
    <div>
      <h2 className="form-title">{title}</h2>
      <div className="form-card">
        <h3 className="form-header">Add New</h3>
        {error && <p className="error-text">{error}</p>}
        <form onSubmit={handleCreate} className="grid-form">
          {fields.map(f => (
            f.type === 'select' ? (
              <select className="form-input" key={f.name} required={f.required !== false} onChange={e => setForm({ ...form, [f.name]: e.target.value })}>
                <option value="">Select {f.label}</option>
                {f.options.map(o => <option key={o.value || o} value={o.value || o}>{o.label || o}</option>)}
              </select>
            ) : (
              <input className="form-input" key={f.name} placeholder={f.label} required={f.required !== false} type={f.type || 'text'} onChange={e => setForm({ ...form, [f.name]: e.target.value })} />
            )
          ))}
          <button className="submit-button" type="submit">Submit</button>
        </form>
      </div>

      {allowFilters && (
        <div style={{ marginBottom: '2rem', display: 'flex', gap: '1rem', background: 'white', padding: '1rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Date Filter</label>
            <input type="date" className="form-input" value={filters.date} onChange={e => setFilters({...filters, date: e.target.value})} />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Equipment Type</label>
            <select className="form-input" value={filters.equipmentTypeId} onChange={e => setFilters({...filters, equipmentTypeId: e.target.value})}>
              <option value="">All Equipment</option>
              {equipmentTypes.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
            </select>
          </div>
        </div>
      )}

      <div className="form-card">
        <h3 className="form-header">Existing Records</h3>
        {items.length === 0 ? (
          <p className="empty-text">No records found.</p>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr className="table-row-head">
                  <th>ID</th>
                  {fields.filter(f => !f.hideInTable).map(f => (
                    <th key={f.name}>{f.label}</th>
                  ))}
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map(item => (
                  <tr key={item.id} className="table-row">
                    <td>{item.id}</td>
                    {fields.filter(f => !f.hideInTable).map(f => (
                      <td key={f.name}>{String(getNestedValue(item, f.name) ?? '')}</td>
                    ))}
                    <td>
                      <button className="delete-button" onClick={() => handleDelete(item.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

function Inventory({ auth }) {
  const [items, setItems] = useState([]);
  const [filters, setFilters] = useState({ date: '', baseId: '', equipmentTypeId: '' });
  const [bases, setBases] = useState([]);
  const [equipmentTypes, setEquipmentTypes] = useState([]);

  useEffect(() => {
    if (auth?.role === 'ADMIN') {
      axios.get(`${API_BASE}/bases`).then(res => setBases(res.data.content || res.data)).catch(console.error);
    }
    axios.get(`${API_BASE}/equipment-types`).then(res => setEquipmentTypes(res.data.content || res.data)).catch(console.error);
  }, [auth]);

  const fetchItems = async () => {
    try {
      const params = new URLSearchParams();
      if (filters.date) params.append('date', filters.date);
      if (filters.baseId) params.append('baseId', filters.baseId);
      if (filters.equipmentTypeId) params.append('equipmentTypeId', filters.equipmentTypeId);
      
      const res = await axios.get(`${API_BASE}/dashboard/inventory?${params.toString()}`);
      if (res.data.content) setItems(res.data.content);
      else if (Array.isArray(res.data)) setItems(res.data);
      else setItems([]);
    } catch (err) {
      console.error('Failed to fetch inventory', err);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [filters]);

  return (
    <div>
      <h2 className="form-title">Inventory</h2>
      <div style={{ marginBottom: '2rem', display: 'flex', gap: '1rem', background: 'white', padding: '1rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Date Filter</label>
          <input type="date" className="form-input" value={filters.date} onChange={e => setFilters({...filters, date: e.target.value})} />
        </div>
        {auth?.role === 'ADMIN' && (
          <div>
            <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Base</label>
            <select className="form-input" value={filters.baseId} onChange={e => setFilters({...filters, baseId: e.target.value})}>
              <option value="">All Bases</option>
              {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
        )}
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Equipment Type</label>
          <select className="form-input" value={filters.equipmentTypeId} onChange={e => setFilters({...filters, equipmentTypeId: e.target.value})}>
            <option value="">All Equipment</option>
            {equipmentTypes.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
          </select>
        </div>
      </div>

      <div className="form-card">
        <h3 className="form-header">Inventory Records</h3>
        {items.length === 0 ? (
          <p className="empty-text">No records found.</p>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr className="table-row-head">
                  <th>Base</th>
                  <th>Equipment</th>
                  <th>Opening</th>
                  <th>Purchases</th>
                  <th>Transfers In</th>
                  <th>Transfers Out</th>
                  <th>Expended</th>
                  <th>Assigned</th>
                  <th>Closing</th>
                  <th>Available</th>
                  <th>Unit</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item, idx) => (
                  <tr key={idx} className="table-row">
                    <td>{item.baseName}</td>
                    <td>{item.equipmentName}</td>
                    <td>{item.openingBalance}</td>
                    <td>{item.purchases}</td>
                    <td>{item.transfersIn}</td>
                    <td>{item.transfersOut}</td>
                    <td>{item.expended}</td>
                    <td>{item.assigned}</td>
                    <td>{item.closingBalance}</td>
                    <td>{item.available}</td>
                    <td>{item.unit}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

function AuditLogs({ auth }) {
  const [items, setItems] = useState([]);
  const [filters, setFilters] = useState({ action: '', entityType: '', baseId: '' });
  const [bases, setBases] = useState([]);

  useEffect(() => {
    if (auth?.role === 'ADMIN') {
      axios.get(`${API_BASE}/bases`).then(res => setBases(res.data.content || res.data)).catch(console.error);
    }
  }, [auth]);

  const fetchItems = async () => {
    try {
      const params = new URLSearchParams();
      if (filters.action) params.append('action', filters.action);
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.baseId) params.append('baseId', filters.baseId);
      
      const res = await axios.get(`${API_BASE}/audit-logs?${params.toString()}`);
      if (res.data.content) setItems(res.data.content);
      else if (Array.isArray(res.data)) setItems(res.data);
      else setItems([]);
    } catch (err) {
      console.error('Failed to fetch audit logs', err);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [filters]);

  return (
    <div>
      <h2 className="form-title">Audit Logs</h2>
      <div style={{ marginBottom: '2rem', display: 'flex', gap: '1rem', background: 'white', padding: '1rem', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Action</label>
          <input type="text" className="form-input" placeholder="e.g. CREATE_TRANSFER" value={filters.action} onChange={e => setFilters({...filters, action: e.target.value})} />
        </div>
        <div>
          <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Entity Type</label>
          <input type="text" className="form-input" placeholder="e.g. TRANSFER" value={filters.entityType} onChange={e => setFilters({...filters, entityType: e.target.value})} />
        </div>
        {auth?.role === 'ADMIN' && (
          <div>
            <label style={{ display: 'block', fontSize: '0.9rem', marginBottom: '0.25rem' }}>Base</label>
            <select className="form-input" value={filters.baseId} onChange={e => setFilters({...filters, baseId: e.target.value})}>
              <option value="">All Bases</option>
              {bases.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
          </div>
        )}
      </div>

      <div className="form-card">
        <h3 className="form-header">Audit Records</h3>
        {items.length === 0 ? (
          <p className="empty-text">No records found.</p>
        ) : (
          <div className="table-container" style={{ overflowX: 'auto' }}>
            <table className="data-table">
              <thead>
                <tr className="table-row-head">
                  <th>Timestamp</th>
                  <th>User</th>
                  <th>Role</th>
                  <th>Session ID</th>
                  <th>Action</th>
                  <th>Entity Type</th>
                  <th>Entity ID</th>
                  <th>Base ID</th>
                  <th>Changes</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item, idx) => (
                  <tr key={idx} className="table-row">
                    <td>{new Date(item.created_at).toLocaleString()}</td>
                    <td>{item.username}</td>
                    <td>{item.role}</td>
                    <td>{item.session_id}</td>
                    <td>{item.action}</td>
                    <td>{item.entity_type}</td>
                    <td>{item.entity_id}</td>
                    <td>{item.base_id}</td>
                    <td style={{ maxWidth: '300px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {item.new_value ? item.new_value : item.old_value}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

function App() {
  const [auth, setAuth] = useState(null);
  const [baseOptions, setBaseOptions] = useState([]);

  useEffect(() => {
    axios.interceptors.request.use(config => {
      const token = localStorage.getItem('token');
      if (token) config.headers.Authorization = `Bearer ${token}`;
      return config;
    });
    const token = localStorage.getItem('token');
    if (token) {
      axios.get(`${API_BASE}/auth/me`).then(res => setAuth({ ...res.data, role: res.data.role || res.data.roleName })).catch(() => localStorage.removeItem('token'));
    }
  }, []);

  useEffect(() => {
    if (auth) {
      axios.get(`${API_BASE}/bases`).then(res => {
        const b = res.data.content || res.data;
        setBaseOptions(b.map(base => ({ value: base.id, label: `${base.name} (${base.code})` })));
      }).catch(console.error);
    }
  }, [auth]);

  const logout = () => {
    localStorage.removeItem('token');
    setAuth(null);
  };

  const rankOptions = ['Private', 'Corporal', 'Sergeant', 'Lieutenant', 'Captain', 'Major', 'Colonel', 'General'];
  const roleOptions = [{ value: 1, label: 'Admin' }, { value: 2, label: 'Base Commander' }, { value: 3, label: 'Logistics Officer' }];
  const categoryOptions = ['WEAPON', 'VEHICLE', 'COMMUNICATION', 'MEDICAL', 'GEAR'];

  return (
    <Router>
      <Routes>
        <Route path="/login" element={!auth ? <Login setAuth={setAuth} /> : <Navigate to="/" />} />
        <Route path="/" element={auth ? <Layout auth={auth} logout={logout}><Dashboard auth={auth} /></Layout> : <Navigate to="/login" />} />
        <Route path="/inventory" element={auth ? <Layout auth={auth} logout={logout}><Inventory auth={auth} /></Layout> : <Navigate to="/login" />} />
        <Route path="/audit-logs" element={auth ? <Layout auth={auth} logout={logout}><AuditLogs auth={auth} /></Layout> : <Navigate to="/login" />} />
        <Route path="/bases" element={auth?.role === 'ADMIN' ? <Layout auth={auth} logout={logout}><GenericForm title="Add Base (Admin)" endpoint="bases" fields={[{ name: 'code', label: 'Base Code' }, { name: 'name', label: 'Base Name' }, { name: 'location', label: 'Location' }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/users" element={auth?.role === 'ADMIN' ? <Layout auth={auth} logout={logout}><GenericForm title="Add System User (Admin)" endpoint="users" fields={[{ name: 'username', label: 'Username' }, { name: 'email', label: 'Email' }, { name: 'passwordHash', label: 'Password (Plain)', type: 'password', hideInTable: true }, { name: 'role.id', label: 'Role', type: 'select', options: roleOptions }, { name: 'baseId', label: 'Base ID (Leave blank for Admin)', type: 'select', options: baseOptions, required: false }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/equipment-types" element={auth?.role === 'ADMIN' ? <Layout auth={auth} logout={logout}><GenericForm title="Add Equipment Type" endpoint="equipment-types" fields={[{ name: 'name', label: 'Name' }, { name: 'category', label: 'Category', type: 'select', options: categoryOptions }, { name: 'unit', label: 'Unit (e.g. piece, kg)' }, { name: 'description', label: 'Description' }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/personnel" element={(auth?.role === 'ADMIN' || auth?.role === 'BASE_COMMANDER') ? <Layout auth={auth} logout={logout}><GenericForm title="Add Personnel" endpoint="personnel" fields={[{ name: 'serviceNumber', label: 'Service Number' }, { name: 'name', label: 'Full Name' }, { name: 'rankName', label: 'Rank', type: 'select', options: rankOptions }, { name: 'baseId', label: 'Base', type: 'select', options: baseOptions }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/transfers" element={(auth?.role === 'ADMIN' || auth?.role === 'LOGISTICS_OFFICER' || auth?.role === 'BASE_COMMANDER') ? <Layout auth={auth} logout={logout}><GenericForm title="Transfers" endpoint="transfers" fields={[{ name: 'sourceBaseId', label: 'Source Base', type: 'select', options: baseOptions }, { name: 'destinationBaseId', label: 'Destination Base', type: 'select', options: baseOptions }, { name: 'equipmentTypeId', label: 'Equipment Type ID', type: 'number' }, { name: 'quantity', label: 'Quantity', type: 'number' }, { name: 'transferDate', label: 'Transfer Date', type: 'date' }, { name: 'referenceNumber', label: 'Ref Number' }, { name: 'notes', label: 'Notes' }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/purchases" element={(auth?.role === 'ADMIN' || auth?.role === 'LOGISTICS_OFFICER' || auth?.role === 'BASE_COMMANDER') ? <Layout auth={auth} logout={logout}><GenericForm title="Purchases" endpoint="purchases" allowFilters={true} fields={[{ name: 'baseId', label: 'Base', type: 'select', options: baseOptions }, { name: 'equipmentTypeId', label: 'Equipment Type ID', type: 'number' }, { name: 'quantity', label: 'Quantity', type: 'number' }, { name: 'purchaseDate', label: 'Purchase Date', type: 'date' }, { name: 'supplier', label: 'Supplier' }, { name: 'referenceNumber', label: 'Ref Number' }, { name: 'notes', label: 'Notes' }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/assignments" element={(auth?.role === 'ADMIN' || auth?.role === 'BASE_COMMANDER') ? <Layout auth={auth} logout={logout}><GenericForm title="Assignments" endpoint="assignments" fields={[{ name: 'baseId', label: 'Base', type: 'select', options: baseOptions }, { name: 'personnelId', label: 'Personnel ID', type: 'number' }, { name: 'equipmentTypeId', label: 'Equipment Type ID', type: 'number' }, { name: 'quantity', label: 'Quantity', type: 'number' }, { name: 'assignmentDate', label: 'Assignment Date', type: 'date' }]} /></Layout> : <Navigate to="/" />} />
        <Route path="/expenditures" element={(auth?.role === 'ADMIN' || auth?.role === 'BASE_COMMANDER') ? <Layout auth={auth} logout={logout}><GenericForm title="Expenditures" endpoint="expenditures" fields={[{ name: 'baseId', label: 'Base', type: 'select', options: baseOptions }, { name: 'equipmentTypeId', label: 'Equipment Type ID', type: 'number' }, { name: 'quantity', label: 'Quantity', type: 'number' }, { name: 'expenditureDate', label: 'Expenditure Date', type: 'date' }, { name: 'category', label: 'Category' }, { name: 'referenceNumber', label: 'Ref Number' }, { name: 'notes', label: 'Notes' }]} /></Layout> : <Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;
