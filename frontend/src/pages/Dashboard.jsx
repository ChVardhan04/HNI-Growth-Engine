import { useEffect, useState } from 'react';
import api from '../api/axios';
import {
  BarChart, Bar, PieChart, Pie, Cell, LineChart, Line, AreaChart, Area,
  XAxis, YAxis, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';

const TIER_COLORS = ['#16a34a', '#2563eb', '#0891b2', '#f59e0b', '#9ca3af'];
const BAND_COLORS = ['#94a3b8', '#64748b', '#d97706', '#ea580c', '#dc2626'];

function Metric({ label, value }) {
  return (
    <div className="card-metric">
      <div className="label">{label}</div>
      <div className="value">{value}</div>
    </div>
  );
}

function mapToArray(obj) {
  return obj ? Object.entries(obj).map(([name, value]) => ({ name, value })) : [];
}

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [provider, setProvider] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([api.get('/dashboard'), api.get('/ai/provider')])
      .then(([d, p]) => {
        setStats(d.data.data);
        setProvider(p.data.data.activeProvider);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="row g-3">
        {Array.from({ length: 8 }).map((_, i) => (
          <div className="col-md-3" key={i}><div className="skeleton" style={{ height: 90 }} /></div>
        ))}
      </div>
    );
  }
  if (!stats) return <div>Could not load dashboard.</div>;

  const tierData = mapToArray(stats.leadsByTier);
  const bandData = mapToArray(stats.leadsByIntentBand);
  const statusData = mapToArray(stats.leadsByStatus);
  const funnelData = [
    { name: 'Total', value: stats.totalLeads },
    { name: 'Qualified', value: stats.qualifiedLeads },
    { name: 'Sales Ready', value: stats.salesReadyLeads },
    { name: 'Converted', value: stats.convertedLeads },
  ];

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h4 style={{ fontWeight: 700 }}>Dashboard</h4>
        <span className="badge bg-primary">AI Provider: {provider}</span>
      </div>

      <div className="row g-3 mb-3">
        <div className="col-md-3"><Metric label="Total Leads" value={stats.totalLeads} /></div>
        <div className="col-md-3"><Metric label="Qualified" value={stats.qualifiedLeads} /></div>
        <div className="col-md-3"><Metric label="Hot Leads" value={stats.hotLeads} /></div>
        <div className="col-md-3"><Metric label="Sales Ready" value={stats.salesReadyLeads} /></div>
        <div className="col-md-3"><Metric label="Conversion Rate" value={stats.conversionRate + '%'} /></div>
        <div className="col-md-3"><Metric label="Predicted Conversion" value={stats.predictedConversionRate + '%'} /></div>
        <div className="col-md-3"><Metric label="Revenue Forecast" value={'₹' + Number(stats.revenueForecast).toLocaleString()} /></div>
        <div className="col-md-3"><Metric label="Pending Approvals" value={stats.pendingApprovals} /></div>
      </div>

      <div className="row g-3 mb-3">
        <div className="col-md-6">
          <div className="panel">
            <h6 className="mb-3">Leads by Tier</h6>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={tierData}>
                <XAxis dataKey="name" /><YAxis allowDecimals={false} /><Tooltip />
                <Bar dataKey="value">
                  {tierData.map((e, i) => <Cell key={i} fill={TIER_COLORS[i % TIER_COLORS.length]} />)}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="col-md-6">
          <div className="panel">
            <h6 className="mb-3">Intent Band Distribution</h6>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={bandData} dataKey="value" nameKey="name" outerRadius={90} label>
                  {bandData.map((e, i) => <Cell key={i} fill={BAND_COLORS[i % BAND_COLORS.length]} />)}
                </Pie>
                <Legend /><Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="row g-3 mb-3">
        <div className="col-md-6">
          <div className="panel">
            <h6 className="mb-3">Pipeline Funnel</h6>
            <ResponsiveContainer width="100%" height={240}>
              <AreaChart data={funnelData}>
                <XAxis dataKey="name" /><YAxis allowDecimals={false} /><Tooltip />
                <Area type="monotone" dataKey="value" stroke="#1e5eff" fill="#1e5eff33" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
        <div className="col-md-6">
          <div className="panel">
            <h6 className="mb-3">Leads by Status</h6>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={statusData}>
                <XAxis dataKey="name" hide /><YAxis allowDecimals={false} /><Tooltip />
                <Line type="monotone" dataKey="value" stroke="#0891b2" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="row g-3">
        <div className="col-md-7">
          <div className="panel">
            <h6 className="mb-3">Top Prospects</h6>
            <table className="table-clean">
              <thead><tr><th>Name</th><th>Company</th><th>ICP</th><th>Tier</th><th>Intent</th></tr></thead>
              <tbody>
                {stats.topProspects.map((l) => (
                  <tr key={l.id}>
                    <td>{l.name}</td><td>{l.company}</td><td>{l.icpScore}</td>
                    <td><span className={'badge-tier tier-' + l.tier}>{l.tier}</span></td>
                    <td><span className={'band band-' + l.intentBand}>{l.intentBand}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        <div className="col-md-5">
          <div className="panel">
            <h6 className="mb-3">Recent Audit Logs</h6>
            <table className="table-clean">
              <thead><tr><th>Action</th><th>Entity</th><th>By</th></tr></thead>
              <tbody>
                {stats.recentAuditLogs.map((a) => (
                  <tr key={a.id}>
                    <td>{a.action}</td><td>{a.entityType} #{a.entityId}</td><td>{a.performedBy}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
