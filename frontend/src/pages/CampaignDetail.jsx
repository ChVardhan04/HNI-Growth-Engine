import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../api/axios';

export default function CampaignDetail() {
  const { id } = useParams();
  const [campaign, setCampaign] = useState(null);
  const [leads, setLeads] = useState([]);

  useEffect(() => {
    api.get(`/campaigns/${id}`).then((r) => setCampaign(r.data.data));
    api.get('/leads', { params: { size: 100 } }).then((r) =>
      setLeads(r.data.data.content.filter((l) => l.campaignId === Number(id)))
    );
  }, [id]);

  if (!campaign) return <div className="skeleton" style={{ height: 200 }} />;

  return (
    <div>
      <h4 style={{ fontWeight: 700 }} className="mb-3">{campaign.name}</h4>
      <div className="panel mb-3">
        <div className="row">
          <div className="col-md-3"><strong>Status</strong><div>{campaign.status}</div></div>
          <div className="col-md-3"><strong>Industry</strong><div>{campaign.industry || '-'}</div></div>
          <div className="col-md-3"><strong>Designation</strong><div>{campaign.designation || '-'}</div></div>
          <div className="col-md-3"><strong>Location</strong><div>{campaign.location || '-'}</div></div>
          <div className="col-md-3 mt-2"><strong>Leads Imported</strong><div>{campaign.leadsImported}</div></div>
        </div>
      </div>
      <div className="panel">
        <h6 className="mb-3">Imported Leads</h6>
        <table className="table-clean">
          <thead><tr><th>Name</th><th>Company</th><th>ICP</th><th>Tier</th><th></th></tr></thead>
          <tbody>
            {leads.map((l) => (
              <tr key={l.id}>
                <td>{l.name}</td><td>{l.company}</td><td>{l.icpScore}</td>
                <td><span className={'badge-tier tier-' + l.tier}>{l.tier}</span></td>
                <td><Link to={`/leads/${l.id}`}>View</Link></td>
              </tr>
            ))}
            {leads.length === 0 && <tr><td colSpan={5} className="text-muted text-center py-3">No leads imported yet.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
