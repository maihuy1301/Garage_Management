import React from 'react';
import { EmptyState } from '@/components/common/EmptyState';

interface PlaceholderPageProps {
  moduleName: string;
  taskNumber: string;
}

export const PlaceholderPage: React.FC<PlaceholderPageProps> = ({ moduleName, taskNumber }) => {
  return (
    <div className="animate-fade-in">
      <EmptyState
        title={`Module ${moduleName}`}
        description={`Chức năng này sẽ được triển khai chi tiết trong ${taskNumber} theo kế hoạch lộ trình phát triển.`}
      />
    </div>
  );
};
