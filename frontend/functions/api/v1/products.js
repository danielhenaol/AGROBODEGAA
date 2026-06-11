const KONG_PRODUCTS_URL =
  'https://90a76c5879.us.serverless.gateways.konggateway.com/api/v1/products'

function buildResponse(body, status = 200) {
  return new Response(body, {
    status,
    headers: {
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
    },
  })
}

export async function onRequestGet() {
  try {
    const response = await fetch(KONG_PRODUCTS_URL, {
      method: 'GET',
      headers: {
        Accept: 'application/json',
      },
    })

    const body = await response.text()

    return buildResponse(body, response.status)
  } catch (error) {
    return buildResponse(
      JSON.stringify({
        error: 'Error consultando productos desde Cloudflare Function',
        detail: error.message,
      }),
      500
    )
  }
}

export async function onRequestOptions() {
  return new Response(null, {
    status: 204,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
    },
  })
}