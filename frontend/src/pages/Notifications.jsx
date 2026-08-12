import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';

const ICONS = {
  HOT_LEAD: '🔥', MEETING_REQUEST: '📅', REPLY_RECEIVED: '💬',
  FOLLOW_UP_DUE: '⏰', MESSAGE_FAILED: '⚠️', INFO: 'ℹ️',
};

export default function Notifications() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    api.get('/notifications/me').then((r) => setItems(r.data.data)).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const markRead = async (id) => {
    await api.patch(`/notifications/${id}/read`);
    load();
  };

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">Notifications</h4>
      <div className="panel">
        {loading ? (
          <div className="skeleton" style={{ height: 200 }} />
        ) : items.length === 0 ? (
          <div className="text-muted text-center py-4">No notifications yet.</div>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
            {items.map((n) => (
              <li key={n.id} className="d-flex justify-content-between align-items-start py-2"
                  style={{ borderBottom: '1px solid #eee', opacity: n.read ? 0.6 : 1 }}>
                <div>
                  <div><strong>{ICONS[n.type] || 'ℹ️'} {n.title}</strong></div>
                  <div className="text-muted" style={{ fontSize: 13 }}>{n.message}</div>
                  {n.link && <Link to={n.link} style={{ fontSize: 13 }}>View →</Link>}
                </div>
                {!n.read && (
                  <button className="btn btn-sm btn-outline-secondary" onClick={() => markRead(n.id)}>
                    Mark read
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
