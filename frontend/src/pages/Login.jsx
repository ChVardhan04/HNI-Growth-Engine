import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('admin@hnigrowth.com');
  const [password, setPassword] = useState('Admin@123');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/dashboard');
    } catch (e) {
      setError(e.response?.data?.message || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-card">
        <h4 className="mb-1" style={{ fontWeight: 700 }}>HNI AI Growth Engine</h4>
        <p style={{ color: '#6b7688', fontSize: '0.9rem' }}>Sign in to your workspace</p>

        {error && <div className="alert alert-danger py-2">{error}</div>}

        <label className="form-label mt-2">Email</label>
        <input className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} />

        <label className="form-label mt-3">Password</label>
        <input type="password" className="form-control" value={password}
               onChange={(e) => setPassword(e.target.value)}
               onKeyDown={(e) => e.key === 'Enter' && submit()} />

        <button className="btn btn-brand w-100 mt-4" onClick={submit} disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </button>

        {/* <p className="mt-3 mb-0" style={{ fontSize: '0.78rem', color: '#6b7688' }}>
          Seeded admin: admin@hnigrowth.com / Admin@123
        </p> */}
      </div>
    </div>
  );
}
