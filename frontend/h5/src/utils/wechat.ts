/**
 * 微信 JSSDK 工具类
 */

interface WeChatConfig {
  appId: string
  timestamp: string
  nonceStr: string
  signature: string
  jsApiList: string
}

interface ShareData {
  title: string
  desc: string
  link: string
  imgUrl: string
}

/**
 * 加载微信 JSSDK
 */
export function loadWeChatSDK(): Promise<void> {
  return new Promise((resolve, reject) => {
    if ((window as any).wx) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('加载微信 JSSDK 失败'))
    document.head.appendChild(script)
  })
}

/**
 * 获取 JSSDK 配置
 */
export async function getWeChatConfig(url?: string): Promise<WeChatConfig | null> {
  try {
    // 如果没有传入 url，使用当前页面 url（去除 # 后面的部分）
    const requestUrl = url || window.location.href.split('#')[0]

    const response = await fetch('/api/wechat/jsapi-config', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ url: requestUrl })
    })

    const result = await response.json()
    if (result.code === 200) {
      return result.data
    }

    return null
  } catch (error) {
    console.error('获取微信配置失败:', error)
    return null
  }
}

/**
 * 配置微信 JSSDK
 */
export async function configWeChatSDK(apiList: string[] = [
  'updateAppMessageShareData',
  'updateTimelineShareData',
  'onMenuShareAppMessage',
  'onMenuShareTimeline',
  'hideMenuItems',
  'showMenuItems'
]): Promise<boolean> {
  try {
    // 加载 JSSDK
    await loadWeChatSDK()

    // 获取配置
    const config = await getWeChatConfig()
    if (!config) {
      console.error('获取微信配置失败')
      return false
    }

    // 解析 jsApiList
    const jsApiList = config.jsApiList.split(',')

    // 配置 JSSDK
    ;(window as any).wx.config({
      debug: false, // 开发时可以设置为 true 查看调试信息
      appId: config.appId,
      timestamp: config.timestamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList
    })

    return new Promise((resolve) => {
      ;(window as any).wx.ready(() => {
        console.log('微信 JSSDK 配置成功')
        resolve(true)
      })

      ;(window as any).wx.error((res: any) => {
        console.error('微信 JSSDK 配置失败:', res)
        resolve(false)
      })
    })
  } catch (error) {
    console.error('配置微信 JSSDK 失败:', error)
    return false
  }
}

/**
 * 更新分享给朋友的内容
 */
export function updateAppMessageShareData(data: ShareData): void {
  const wx = (window as any).wx
  if (wx && wx.updateAppMessageShareData) {
    wx.updateAppMessageShareData({
      title: data.title,
      desc: data.desc,
      link: data.link,
      imgUrl: data.imgUrl,
      success: () => {
        console.log('分享给朋友设置成功')
      },
      cancel: () => {
        console.log('分享给朋友取消')
      }
    })
  }
}

/**
 * 更新分享到朋友圈的内容
 */
export function updateTimelineShareData(data: ShareData): void {
  const wx = (window as any).wx
  if (wx && wx.updateTimelineShareData) {
    wx.updateTimelineShareData({
      title: data.title,
      link: data.link,
      imgUrl: data.imgUrl,
      success: () => {
        console.log('分享到朋友圈设置成功')
      },
      cancel: () => {
        console.log('分享到朋友圈取消')
      }
    })
  }
}

/**
 * 兼容旧版本的分享接口
 */
export function setOldShareData(data: ShareData): void {
  const wx = (window as any).wx

  if (wx && wx.onMenuShareAppMessage) {
    wx.onMenuShareAppMessage({
      title: data.title,
      desc: data.desc,
      link: data.link,
      imgUrl: data.imgUrl,
      success: () => {
        console.log('分享给朋友设置成功（旧版）')
      }
    })
  }

  if (wx && wx.onMenuShareTimeline) {
    wx.onMenuShareTimeline({
      title: data.title,
      link: data.link,
      imgUrl: data.imgUrl,
      success: () => {
        console.log('分享到朋友圈设置成功（旧版）')
      }
    })
  }
}

/**
 * 设置分享内容（新旧接口都设置）
 */
export function setShareData(data: ShareData): void {
  // 新版本接口
  updateAppMessageShareData(data)
  updateTimelineShareData(data)

  // 旧版本接口（兼容性）
  setOldShareData(data)
}

/**
 * 检测是否在微信环境中
 */
export function isWeChat(): boolean {
  return /micromessenger/i.test(navigator.userAgent)
}

/**
 * 初始化微信分享（在页面加载时调用）
 */
export async function initWeChatShare(data: ShareData): Promise<boolean> {
  if (!isWeChat()) {
    console.log('非微信环境，跳过微信分享配置')
    return false
  }

  try {
    const success = await configWeChatSDK()
    if (success) {
      setShareData(data)
    }
    return success
  } catch (error) {
    console.error('初始化微信分享失败:', error)
    return false
  }
}
