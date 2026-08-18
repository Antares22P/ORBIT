const API_BASE_URL =
  "http://localhost:8080/api";

async function request(
  url,
  options = {}
) {
  const response = await fetch(
    `${API_BASE_URL}${url}`,
    {
      ...options,

      headers: {
        "Content-Type":
          "application/json",

        ...(options.headers || {}),
      },
    }
  );

  const data =
    await response.json();

  if (!response.ok) {
    throw new Error(
      data?.error ||
        data?.message ||
        "Something went wrong"
    );
  }

  return data;
}

// =========================================================
// GROUP
// =========================================================

export async function createGroup(
  name
) {
  return request("/groups", {
    method: "POST",

    body: JSON.stringify({
      name,
    }),
  });
}

export async function getGroup(
  groupId
) {
  return request(
    `/groups/${groupId}`
  );
}

// =========================================================
// MEMBERS
// =========================================================

export async function addMember(
  groupId,
  member
) {
  return request(
    `/groups/${groupId}/members`,
    {
      method: "POST",

      body: JSON.stringify(
        member
      ),
    }
  );
}

export async function getMembers(
  groupId
) {
  return request(
    `/groups/${groupId}/members`
  );
}

// =========================================================
// LOCATION
// =========================================================

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


// =========================================================
// DESTINATION
// =========================================================

export async function setDestination(
  groupId,
  latitude,
  longitude,
  name = "Destination"
) {
  return request(
    `/groups/${groupId}/destination`,
    {
      method: "POST",

      body: JSON.stringify({
        latitude,
        longitude,
        name,
      }),
    }
  );
}

export async function getDestination(
  groupId
) {
  return request(
    `/groups/${groupId}/destination`
  );
}

export async function deleteDestination(
  groupId
) {
  return request(
    `/groups/${groupId}/destination`,
    {
      method: "DELETE",
    }
  );
}

// =========================================================
// DISTANCE
// =========================================================

export async function getDistance(
  latitude1,
  longitude1,
  latitude2,
  longitude2
) {
  return request(
    `/distance?latitude1=${latitude1}&longitude1=${longitude1}&latitude2=${latitude2}&longitude2=${longitude2}`
  );
}


// =========================================================
// MEMBER DISTANCE
// =========================================================

export async function getMemberDistance(groupId, memberId) {
  return request(
    `/groups/${groupId}/members/${memberId}/distance`
  );
}