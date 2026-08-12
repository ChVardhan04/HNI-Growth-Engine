import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dark, setDark] = useState(() => localStorage.getItem('theme') === 'dark');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    localStorage.setItem('theme', dark ? 'dark' : 'light');
  }, [dark]);

  const doLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar-top">
      <div style={{ fontWeight: 600 }}>Sales Intelligence Platform</div>
      <div className="d-flex align-items-center gap-3">
        <button className="btn btn-sm btn-outline-secondary" onClick={() => setDark(!dark)}>
          {dark ? '☀️ Light' : '🌙 Dark'}
        </button>
        <div className="text-end">
          <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{user?.fullName}</div>
          <div style={{ fontSize: '0.72rem', color: 'var(--muted)' }}>{user?.role}</div>
        </div>
        <button className="btn btn-sm btn-brand" onClick={doLogout}>Logout</button>
      </div>
    </header>
  );
}
