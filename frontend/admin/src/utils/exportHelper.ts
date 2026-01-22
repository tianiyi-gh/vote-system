/**
 * 导出工具类
 * 支持CSV和Excel格式导出
 */

interface ExportColumn {
  key: string
  label: string
  width?: number
  visible?: boolean
}

interface ExportOptions {
  filename?: string
  format?: 'csv' | 'excel'
  columns?: ExportColumn[]
  showProgress?: boolean
}

/**
 * CSV导出
 */
export const exportToCSV = (
  data: any[],
  columns: ExportColumn[],
  filename: string = 'export.csv'
) => {
  // 过滤可见列
  const visibleColumns = columns.filter(col => col.visible !== false)

  // CSV头部（UTF-8 BOM）
  let csv = '\uFEFF'
  csv += visibleColumns.map(col => col.label).join(',') + '\n'

  // 数据行
  data.forEach(row => {
    const values = visibleColumns.map(col => {
      const value = getNestedValue(row, col.key)
      return escapeCsvValue(value)
    })
    csv += values.join(',') + '\n'
  })

  // 下载
  downloadBlob(
    new Blob([csv], { type: 'text/csv;charset=utf-8;' }),
    filename
  )
}

/**
 * Excel导出（使用简单HTML表格方式）
 */
export const exportToExcel = (
  data: any[],
  columns: ExportColumn[],
  filename: string = 'export.xlsx'
) => {
  // 过滤可见列
  const visibleColumns = columns.filter(col => col.visible !== false)

  // 创建HTML表格
  let html = '<html><head><meta charset="UTF-8"><style>'
  html += 'table { border-collapse: collapse; }'
  html += 'td, th { border: 1px solid #ccc; padding: 8px; text-align: left; }'
  html += 'th { background-color: #f0f0f0; font-weight: bold; }'
  html += '</style></head><body><table>'

  // 表头
  html += '<thead><tr>'
  visibleColumns.forEach(col => {
    html += `<th>${escapeHtml(col.label)}</th>`
  })
  html += '</tr></thead>'

  // 数据行
  html += '<tbody>'
  data.forEach(row => {
    html += '<tr>'
    visibleColumns.forEach(col => {
      const value = getNestedValue(row, col.key)
      html += `<td>${escapeHtml(value)}</td>`
    })
    html += '</tr>'
  })
  html += '</tbody></table></body></html>'

  // 下载
  downloadBlob(
    new Blob([html], { type: 'application/vnd.ms-excel;charset=utf-8;' }),
    filename
  )
}

/**
 * 导出功能（支持CSV和Excel）
 */
export const exportData = (
  data: any[],
  columns: ExportColumn[],
  options: ExportOptions = {}
) => {
  const {
    filename = `export_${Date.now()}`,
    format = 'csv',
    columns: customColumns,
    showProgress = true
  } = options

  // 使用自定义列或默认列
  const exportColumns = customColumns || columns

  // 根据格式导出
  if (format === 'excel') {
    exportToExcel(data, exportColumns, `${filename}.xls`)
  } else {
    exportToCSV(data, exportColumns, `${filename}.csv`)
  }
}

/**
 * CSV值转义
 */
const escapeCsvValue = (value: any): string => {
  if (value === null || value === undefined) {
    return ''
  }
  const str = String(value)
  if (str.includes(',') || str.includes('"') || str.includes('\n')) {
    return `"${str.replace(/"/g, '""')}"`
  }
  return str
}

/**
 * HTML转义
 */
const escapeHtml = (value: any): string => {
  if (value === null || value === undefined) {
    return ''
  }
  const str = String(value)
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/**
 * 获取嵌套属性值
 */
const getNestedValue = (obj: any, path: string): any => {
  const keys = path.split('.')
  let result = obj
  for (const key of keys) {
    if (result && typeof result === 'object') {
      result = result[key]
    } else {
      return ''
    }
  }
  return result ?? ''
}

/**
 * 下载Blob
 */
const downloadBlob = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

/**
 * 导出列定义生成器
 */
export const createExportColumns = (
  columnDefs: Array<{ key: string; label: string; defaultVisible?: boolean }>
): ExportColumn[] => {
  return columnDefs.map(def => ({
    key: def.key,
    label: def.label,
    visible: def.defaultVisible !== false
  }))
}
