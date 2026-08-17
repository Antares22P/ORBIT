const API_BASE_URL = "http://localhost:8080/api";

async function request(url, options = {}) {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(
      data?.error ||
      data?.message ||
      "Something went wrong"
    );
  }

  return data;
}

export async function createGroup(name) {
  return request("/groups", {
    method: "POST",
    body: JSON.stringify({
      name,
    }),
  });
}

export async function getGroup(groupId) {
  return request(`/groups/${groupId}`);
}

export async function addMember(groupId, member) {
  return request(`/groups/${groupId}/members`, {
    method: "POST",
    body: JSON.stringify(member),
  });
}

export async function getMembers(groupId) {
  return request(`/groups/${groupId}/members`);
}

export async function updateMemberLocation(
  groupId,
  memberId,
  latitude,
  longitude,
  accuracy = 0,
  speed = 0,
  heading = 0
) {
  return request(
    `/groups/${groupId}/members/${memberId}/location`,
    {
      method: "PUT",
      body: JSON.stringify({
        latitude,
        longitude,
        accuracy,
        speed,
        heading,
      }),
    }
  );
}