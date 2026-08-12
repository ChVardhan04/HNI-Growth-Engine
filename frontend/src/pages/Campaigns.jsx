import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

export default function Campaigns() {
  const [campaigns, setCampaigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [startingId, setStartingId] = useState(null);
  const [error, setError] = useState('');

  const load = () => {
    setLoading(true);
    api.get('/campaigns').then((r) => setCampaigns(r.data.data)).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const start = async (id) => {
    setStartingId(id); setError('');
    try {
      await api.post(`/campaigns/${id}/start`);
      load();
    } catch (e) {
      setError(e.response?.data?.message || 'Failed to start campaign.');
    } finally {
      setStartingId(null);
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h4 style={{ fontWeight: 700 }}>Campaigns</h4>
        <Link to="/campaigns/new" className="btn btn-primary btn-sm">+ New Campaign</Link>
      </div>
      {error && <div className="alert alert-danger py-2">{error}</div>}
      <div className="panel">
        {loading ? (
          <div className="skeleton" style={{ height: 200 }} />
        ) : (
          <table className="table-clean">
            <thead>
              <tr>
                <th>Name</th><th>Targeting</th><th>Status</th><th>Leads Imported</th><th></th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map((c) => (
                <tr key={c.id}>
                  <td>
                    <Link to={`/campaigns/${c.id}`}>{c.name}</Link>
                    <div className="text-muted" style={{ fontSize: 12 }}>{c.audience}</div>
                  </td>
                  <td style={{ fontSize: 13 }}>
                    {[c.designation, c.industry, c.location].filter(Boolean).join(' · ') || '-'}
                  </td>
                  <td>
                    <span className={'badge-tier tier-' + c.status}>{c.status}</span>
                    {c.status === 'FAILED' && c.failureReason && (
                      <div className="text-danger" style={{ fontSize: 11, maxWidth: 220 }}>{c.failureReason}</div>
                    )}
                  </td>
                  <td>{c.leadsImported ?? 0}</td>
                  <td>
                    {(c.status === 'DRAFT' || c.status === 'SCHEDULED' || c.status === 'FAILED') && (
                      <button className="btn btn-sm btn-outline-primary" disabled={startingId === c.id}
                        onClick={() => start(c.id)}>
                        {startingId === c.id ? 'Searching LinkedIn…' : (c.status === 'FAILED' ? 'Retry' : 'Launch (Auto LinkedIn Search)')}
                      </button>
                    )}
                    {c.status === 'ACTIVE' && <span className="text-muted">Running…</span>}
                    {c.status === 'COMPLETED' && <span className="text-success">✓ Complete</span>}
                  </td>
                </tr>
              ))}
              {campaigns.length === 0 && (
                <tr><td colSpan={5} className="text-muted text-center py-4">No campaigns yet.</td></tr>
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
