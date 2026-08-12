import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';

const DEFAULT_TEMPLATE = "Hi {{firstName}}, I work with senior leaders like yourself at {{company}} on bespoke wealth strategies. Would love to connect.";

export default function Leads() {
  const [data, setData] = useState({ content: [], totalPages: 0, number: 0 });
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(new Set());
  const [showBulkPanel, setShowBulkPanel] = useState(false);
  const [template, setTemplate] = useState(DEFAULT_TEMPLATE);
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState(null);
  const navigate = useNavigate();

  const load = () => {
    setLoading(true);
    api.get('/leads', { params: { search, page, size: 10 } })
      .then((r) => setData(r.data.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [page]);

  const toggle = (id, e) => {
    e.stopPropagation();
    const next = new Set(selected);
    next.has(id) ? next.delete(id) : next.add(id);
    setSelected(next);
  };

  const toggleAll = () => {
    if (selected.size === data.content.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(data.content.map((l) => l.id)));
    }
  };

  const sendBulkConnections = async () => {
    setSending(true); setResult(null);
    try {
      const { data: res } = await api.post('/messages/bulk-connect', {
        leadIds: Array.from(selected),
        messageTemplate: template,
      });
      setResult(res.data);
      setSelected(new Set());
      load();
    } catch (e) {
      setResult({ error: e.response?.data?.message || 'Bulk send failed.' });
    } finally {
      setSending(false);
    }
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h4 style={{ fontWeight: 700 }}>Leads</h4>
        <Link to="/leads/new" className="btn btn-brand">+ New Lead</Link>
      </div>

      <div className="panel mb-3 d-flex gap-2">
        <input className="form-control" placeholder="Search by name, company or email…"
               value={search} onChange={(e) => setSearch(e.target.value)}
               onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
        <button className="btn btn-brand" onClick={() => { setPage(0); load(); }}>Search</button>
      </div>

      {selected.size > 0 && (
        <div className="panel mb-3">
          <div className="d-flex justify-content-between align-items-center">
            <span><b>{selected.size}</b> lead(s) selected</span>
            <div className="d-flex gap-2">
              <button className="btn btn-sm btn-outline-secondary" onClick={() => setSelected(new Set())}>Clear</button>
              <button className="btn btn-sm btn-primary" onClick={() => setShowBulkPanel(!showBulkPanel)}>
                Send Connection Requests to All Selected
              </button>
            </div>
          </div>
          {showBulkPanel && (
            <div className="mt-3">
              <label className="form-label">Default message (sent to everyone selected)</label>
              <textarea className="form-control" rows={3} value={template} onChange={(e) => setTemplate(e.target.value)} />
              <div className="text-muted mt-1" style={{ fontSize: 12 }}>
                Placeholders: {'{{firstName}}'}, {'{{name}}'}, {'{{company}}'}, {'{{designation}}'} - filled in per lead.
              </div>
              <button className="btn btn-primary mt-2" disabled={sending} onClick={sendBulkConnections}>
                {sending ? `Sending to ${selected.size} leads…` : `Send to ${selected.size} Leads`}
              </button>
            </div>
          )}
          {result && !result.error && (
            <div className="alert alert-info mt-3 mb-0">
              Sent {result.succeeded}/{result.total} connection requests
              {result.failed > 0 && ` (${result.failed} failed - check audit log for details)`}.
            </div>
          )}
          {result?.error && <div className="alert alert-danger mt-3 mb-0">{result.error}</div>}
        </div>
      )}

      <div className="panel">
        {loading ? (
          <div className="skeleton" style={{ height: 260 }} />
        ) : (
          <table className="table-clean">
            <thead>
              <tr>
                <th><input type="checkbox" checked={selected.size > 0 && selected.size === data.content.length} onChange={toggleAll} /></th>
                <th>Name</th><th>Company</th><th>Designation</th><th>ICP</th><th>Tier</th><th>Intent</th><th>Status</th><th>RM</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((l) => (
                <tr key={l.id} style={{ cursor: 'pointer' }} onClick={() => navigate('/leads/' + l.id)}>
                  <td onClick={(e) => e.stopPropagation()}>
                    <input type="checkbox" checked={selected.has(l.id)} onChange={(e) => toggle(l.id, e)} />
                  </td>
                  <td>{l.name}</td>
                  <td>{l.company}</td>
                  <td>{l.designation}</td>
                  <td>{l.icpScore}</td>
                  <td><span className={'badge-tier tier-' + l.tier}>{l.tier}</span></td>
                  <td><span className={'band band-' + l.intentBand}>{l.intentBand} · {l.intentScore}</span></td>
                  <td>{l.status}</td>
                  <td>{l.assignedRmName || '-'}</td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr><td colSpan={9} className="text-center text-muted py-4">No leads found.</td></tr>
              )}
            </tbody>
          </table>
        )}

        <div className="d-flex justify-content-between align-items-center mt-3">
          <button className="btn btn-sm btn-outline-secondary" disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}>Previous</button>
          <span style={{ fontSize: '0.85rem', color: 'var(--muted)' }}>
            Page {data.number + 1} of {Math.max(1, data.totalPages)}
          </span>
          <button className="btn btn-sm btn-outline-secondary" disabled={page + 1 >= data.totalPages}
                  onClick={() => setPage((p) => p + 1)}>Next</button>
        </div>
      </div>
    </div>
  );
}
