class GarageNotification {
  const GarageNotification({
    required this.id,
    required this.title,
    required this.content,
    required this.isRead,
    required this.createdAt,
  });

  final int id;
  final String title;
  final String content;
  final bool isRead;
  final DateTime? createdAt;

  factory GarageNotification.fromJson(Map<String, dynamic> json) =>
      GarageNotification(
        id: json['maThongBao'] as int,
        title: json['tieuDe'] as String? ?? 'Thông báo',
        content: json['noiDung'] as String? ?? '',
        isRead: json['daDoc'] as bool? ?? false,
        createdAt: DateTime.tryParse(json['ngayTao'] as String? ?? ''),
      );
}

class NotificationSnapshot {
  const NotificationSnapshot(this.items, this.unreadCount);
  final List<GarageNotification> items;
  final int unreadCount;
}
